import cv2
import pickle
import os
import numpy as np
from dataclasses import dataclass, field
from typing import Dict, List, Tuple, Optional

import torch
import torch.nn as nn
import torchvision.transforms as T
from torchvision.models import mobilenet_v3_small

import keyboard  # pip install keyboard
from PIL import ImageFont, ImageDraw, Image  # для кириллицы

try:
    from ultralytics import YOLO
    YOLO_AVAILABLE = True
except Exception:
    YOLO_AVAILABLE = False

# ================== Конфиг ==================
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
CAM_INDEX = "http://raspberrypi.local:1181/?action=stream"
INFER_SIZE = 224
SAMPLES_PER_ADD = 64              # собираем 64 кадра
UNKNOWN_THRESHOLD = 0.55          # для классификатора
DISPLAY_THRESHOLD = 0.8           # порог отображения результата
MEMORY_FILE = "memory.pkl"
CROP_SIZE = 256                   # размер центральной рамки (меняется + / -)

# ================== Шрифт для кириллицы ==================
# ⚠️ замени путь на подходящий для своей системы
FONT_PATH = "C:/Windows/Fonts/arial.ttf"
font = ImageFont.truetype(FONT_PATH, 24)

def draw_text(img, text, pos=(20,40), color=(0,255,0)):
    """Рисует кириллицу на кадре"""
    img_pil = Image.fromarray(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    draw = ImageDraw.Draw(img_pil)
    draw.text(pos, text, font=font, fill=color[::-1])  # RGB → BGR
    return cv2.cvtColor(np.array(img_pil), cv2.COLOR_RGB2BGR)

# ================== Аугментации ==================
base_tf = T.Compose([
    T.ToPILImage(),
    T.Resize((INFER_SIZE, INFER_SIZE)),
    T.ToTensor(),
    T.Normalize(mean=[0.485,0.456,0.406], std=[0.229,0.224,0.225]),
])

# ================== Модель ==================
class Embedder(nn.Module):
    def __init__(self):
        super().__init__()
        m = mobilenet_v3_small(weights="MobileNet_V3_Small_Weights.DEFAULT")
        self.feature_dim = m.classifier[0].in_features
        self.backbone = nn.Sequential(
            m.features,
            nn.AdaptiveAvgPool2d(1),
            nn.Flatten()
        )
        for p in self.backbone.parameters():
            p.requires_grad = False

    @torch.inference_mode()
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return self.backbone(x)

# ================== Память ==================
@dataclass
class ClassMemory:
    name: str
    embeddings: List[np.ndarray] = field(default_factory=list)
    prototype: Optional[np.ndarray] = None

class MemoryBank:
    def __init__(self):
        self.classes: Dict[str, ClassMemory] = {}

    def add(self, name: str, embs: np.ndarray):
        if name not in self.classes:
            self.classes[name] = ClassMemory(name)
        for e in embs:
            self.classes[name].embeddings.append(e)
        self.recompute_prototype(name)

    def recompute_prototype(self, name: str):
        c = self.classes[name]
        if len(c.embeddings) > 0:
            mat = np.stack(c.embeddings, axis=0)
            c.prototype = mat.mean(axis=0) / (np.linalg.norm(mat.mean(axis=0)) + 1e-9)

    def predict(self, emb: np.ndarray, threshold: float = UNKNOWN_THRESHOLD) -> Tuple[str, float]:
        if not self.classes:
            return "no-classes", 0.0
        best_name, best_sim = "unknown", -1.0
        for name, c in self.classes.items():
            if c.prototype is None: continue
            sim = float(np.dot(emb, c.prototype))
            if sim > best_sim:
                best_sim = sim
                best_name = name
        if best_sim < threshold:
            return "unknown", best_sim
        return best_name, best_sim

    def save(self, path: str):
        with open(path, "wb") as f:
            pickle.dump(self, f)

    @staticmethod
    def load(path: str) -> "MemoryBank":
        with open(path, "rb") as f:
            return pickle.load(f)

# ================== Вспомогательные ==================
def to_embedding(model: Embedder, imgs: List[np.ndarray]) -> np.ndarray:
    if not imgs: return np.empty((0, model.feature_dim), dtype=np.float32)
    tens = torch.stack([base_tf(i) for i in imgs]).to(DEVICE)
    with torch.inference_mode():
        emb = model(tens).cpu().numpy()
    emb = emb / (np.linalg.norm(emb, axis=1, keepdims=True) + 1e-9)
    return emb.astype(np.float32)

def get_crop_with_yolo(frame, det_model):
    global CROP_SIZE
    h, w, _ = frame.shape
    cx, cy = w // 2, h // 2
    half = CROP_SIZE // 2

    # координаты центрального ROI
    x1 = max(0, cx - half)
    y1 = max(0, cy - half)
    x2 = min(w, cx + half)
    y2 = min(h, cy + half)

    roi = frame[y1:y2, x1:x2]
    crop = None

    if det_model is not None:
        results = det_model.predict(roi, conf=0.3, verbose=False)
        if results and len(results[0].boxes) > 0:
            boxes = results[0].boxes.xyxy.cpu().numpy().astype(int)
            areas = [(bx2-bx1)*(by2-by1) for (bx1,by1,bx2,by2) in boxes]
            i = np.argmax(areas)
            bx1, by1, bx2, by2 = boxes[i]
            crop = roi[by1:by2, bx1:bx2]
            # рамка YOLO (зелёная)
            cv2.rectangle(frame, (x1+bx1, y1+by1), (x1+bx2, y1+by2), (0,255,0), 2)
            frame = draw_text(frame, "YOLO", (x1+bx1, y1+by1-25), (0,255,0))

    if crop is None:
        crop = roi

    # рамка ROI (жёлтая)
    cv2.rectangle(frame, (x1,y1), (x2,y2), (0,255,255), 2)
    return crop

# ================== Сбор примеров ==================
def collect_and_add(bank: MemoryBank, model: Embedder, cap: cv2.VideoCapture, name: str, det_model=None):
    print(f"[INFO] Собираю {SAMPLES_PER_ADD} примеров для '{name}'. Держите объект в центральной рамке.")
    grabbed = []
    frame_delay = 5
    counter = 0

    while len(grabbed) < SAMPLES_PER_ADD:
        ok, frame = cap.read()
        if not ok: 
            continue

        counter += 1
        if counter % frame_delay != 0:
            continue

        crop = get_crop_with_yolo(frame, det_model)
        grabbed.append(cv2.cvtColor(crop, cv2.COLOR_BGR2RGB))

        vis = frame.copy()
        vis = draw_text(vis, f"Сбор {name}: {len(grabbed)}/{SAMPLES_PER_ADD}", (20,40), (0,255,255))

        cv2.imshow("Teach&Recognize (основное)", vis)
        cv2.imshow("Crop (идёт в обучение)", crop)

        cv2.waitKey(1)

        if keyboard.is_pressed('x') or keyboard.is_pressed('esc'):
            print("[CANCEL] Обучение отменено пользователем")
            return  # выход без сохранения

    embs = to_embedding(model, grabbed)
    bank.add(name, embs)
    print(f"[OK] Добавлено {len(embs)} примеров для '{name}'")

# ================== Основной цикл ==================
def main():
    global UNKNOWN_THRESHOLD, CROP_SIZE
    print(f"[INFO] Torch device: {DEVICE}")
    model = Embedder().to(DEVICE).eval()
    bank = MemoryBank()

    det_model = None
    if YOLO_AVAILABLE:
        try:
            det_model = YOLO("yolov8n.pt")
            print("[INFO] YOLOv8n загружен — работает в пределах центральной рамки.")
        except Exception as e:
            print("[WARN] YOLO не загрузился:", e)

    cap = cv2.VideoCapture(CAM_INDEX)
    if not cap.isOpened():
        print("[ERR] Камера не найдена")
        return

    last_class_name = None
    print_controls()

    while True:
        ok, frame = cap.read()
        if not ok: break

        crop = get_crop_with_yolo(frame.copy(), det_model)
        embs = to_embedding(model, [cv2.cvtColor(crop, cv2.COLOR_BGR2RGB)])

        if bank.classes:
            name, sim = bank.predict(embs[0], UNKNOWN_THRESHOLD)
        else:
            name, sim = "no-classes", 0.0

        display_name = name if sim >= DISPLAY_THRESHOLD else "неизвестно"

        vis = frame.copy()
        vis = draw_text(vis, f"{display_name} {sim:.2f}", (20,40), (0,255,0))

        cv2.imshow("Teach&Recognize (основное)", vis)
        cv2.imshow("Crop (идёт в обучение)", crop)

        cv2.waitKey(1)

        if keyboard.is_pressed('0'):
            print("[EXIT] Завершение работы")
            break

        elif keyboard.is_pressed('1'):
            print("\n[INPUT] Название нового класса (или 'cancel' для отмены): ", end="", flush=True)
            name = input().strip()
            if not name or name.lower() == "cancel":
                print("[INFO] Добавление класса отменено")
                continue
            last_class_name = name
            collect_and_add(bank, model, cap, name, det_model)

        elif keyboard.is_pressed('2'):
            if not last_class_name:
                print("[WARN] Сначала добавьте класс (1).")
            else:
                collect_and_add(bank, model, cap, last_class_name, det_model)

        elif keyboard.is_pressed('3'):
            bank.save(MEMORY_FILE)
            print(f"[OK] Сохранено в {MEMORY_FILE}")

        elif keyboard.is_pressed('4'):
            if os.path.exists(MEMORY_FILE):
                bank = MemoryBank.load(MEMORY_FILE)
                print(f"[OK] Загружено из {MEMORY_FILE}. Классов: {len(bank.classes)}")
            else:
                print("[WARN] Файл не найден")

        elif keyboard.is_pressed('+') or keyboard.is_pressed('='):
            CROP_SIZE = min(CROP_SIZE + 32, min(frame.shape[0], frame.shape[1]))
            print(f"[INFO] Новый размер рамки: {CROP_SIZE}")

        elif keyboard.is_pressed('-'):
            CROP_SIZE = max(64, CROP_SIZE - 32)
            print(f"[INFO] Новый размер рамки: {CROP_SIZE}")

        elif keyboard.is_pressed('9'):
            print("[INFO] Перезагрузка окон...")
            cv2.destroyAllWindows()
            cv2.waitKey(1)

def print_controls():
    print(
        "\n[УПРАВЛЕНИЕ]\n"
        " 1 — добавить НОВЫЙ класс (или 'cancel' для отмены)\n"
        " 2 — добавить ЕЩЁ примеров к последнему классу\n"
        " 3 — сохранить память (memory.pkl)\n"
        " 4 — загрузить память (memory.pkl)\n"
        " + / - — увеличить или уменьшить рамку ROI\n"
        " x / Esc — отмена во время обучения\n"
        " 9 — перезагрузить окна\n"
        " 0 — выход\n"
    )

if __name__ == "__main__":
    main()
