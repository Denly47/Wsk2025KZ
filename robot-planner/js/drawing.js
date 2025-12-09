/* drawing.js
   Рендер: фон, сетка, линейки, объекты, точки, направления и прямоугольник робота.
*/
window.App = window.App || {};
const state = App.state;
const robot = App.robot;

const canvas = document.getElementById('fieldCanvas');
const ctx = canvas.getContext('2d');

/* Вспомогательные конвертеры см ↔ px */
function pxPerCmX(){ return state.canvasWpx / state.fieldWcm; }
function pxPerCmY(){ return state.canvasHpx / state.fieldHcm; }
function cmToPxX(x){ return x * pxPerCmX(); }
function cmToPxY(y){ return state.canvasHpx - y * pxPerCmY(); }

function round(v,d=1){ const m=10**d; return Math.round(v*m)/m; }
App._geom = { round };

/* Углы/ориентация */
function normDeg(a){ a%=360; if(a<0)a+=360; return a; }
function norm180(a){
  a = ((a + 180) % 360 + 360) % 360 - 180;
  return a;
}

/* Преобразование логического heading в экранный угол с учётом ориентации 0° робота */
function applyZeroHeading(h){
  h = norm180(h);  // логический угол
  switch (robot.zero){
    case 'down':  return normDeg(h + 90);
    case 'up':    return normDeg(h - 90);
    case 'right': return normDeg(h);
    case 'left':  return normDeg(h + 180);
    default:      return normDeg(h);
  }
}

/* Отрисовка сетки (адаптивный шаг по размерам поля) */
function chooseGridStep(cm){
  if(cm<=100) return 10;
  if(cm<=200) return 20;
  if(cm<=400) return 50;
  return 100;
}

function clear(){ ctx.clearRect(0,0,canvas.width,canvas.height); }

function drawGrid(){
  const stepXcm = chooseGridStep(state.fieldWcm);
  const stepYcm = chooseGridStep(state.fieldHcm);
  const stepXpx = stepXcm * pxPerCmX();
  const stepYpx = stepYcm * pxPerCmY();
  ctx.save();
  ctx.strokeStyle = '#e9eefb'; ctx.lineWidth = 1;

  // вертикали
  for(let x=0; x<=state.canvasWpx+0.5; x+=stepXpx){
    ctx.beginPath(); ctx.moveTo(x+0.5, 0.5); ctx.lineTo(x+0.5, state.canvasHpx-0.5); ctx.stroke();
  }
  // горизонтали
  for(let y=0; y<=state.canvasHpx+0.5; y+=stepYpx){
    ctx.beginPath(); ctx.moveTo(0.5, y+0.5); ctx.lineTo(state.canvasWpx-0.5, y+0.5); ctx.stroke();
  }
  ctx.restore();

  // рамка
  ctx.beginPath(); ctx.rect(0.5,0.5,state.canvasWpx-1,state.canvasHpx-1);
  ctx.strokeStyle='#e0e6f8'; ctx.stroke();

  // подпись нуля
  ctx.fillStyle='#111'; ctx.font='12px sans-serif';
  ctx.fillText('(0,0)', 6, state.canvasHpx - 6);
}

/* Линейки по X и Y */
function drawXRuler(){
  const step = chooseGridStep(state.fieldWcm);
  ctx.save();
  ctx.fillStyle='#444';
  ctx.font='10px sans-serif';
  for(let x=0; x<=state.fieldWcm+0.001; x+=step){
    const px = cmToPxX(x);
    ctx.fillText(String(x), px+2, state.canvasHpx-4);
  }
  ctx.restore();
}

function drawYRuler(){
  const step = chooseGridStep(state.fieldHcm);
  ctx.save();
  ctx.fillStyle='#444';
  ctx.font='10px sans-serif';
  for(let y=0; y<=state.fieldHcm+0.001; y+=step){
    const py = cmToPxY(y);
    ctx.fillText(String(y), 4, py-2);
  }
  ctx.restore();
}

/* Стрелка направления */
function drawArrow(x1,y1,x2,y2,w){
  ctx.beginPath(); ctx.moveTo(x1,y1); ctx.lineTo(x2,y2);
  ctx.strokeStyle='#0b5fff'; ctx.lineWidth=2; ctx.stroke();
  const a = Math.atan2(y2-y1, x2-x1);
  ctx.beginPath(); ctx.moveTo(x2,y2);
  ctx.lineTo(x2 - w*Math.cos(a-Math.PI/6), y2 - w*Math.sin(a-Math.PI/6));
  ctx.lineTo(x2 - w*Math.cos(a+Math.PI/6), y2 - w*Math.sin(a+Math.PI/6));
  ctx.closePath(); ctx.fillStyle='#0b5fff'; ctx.fill();
}

/* Статичные объекты (стены/полки/кубики/цилиндры/зоны) */
function drawObjects(){
  if(!state.objects || state.objects.length===0) return;

  ctx.save();
  ctx.font = '11px sans-serif';
  ctx.textAlign = 'center';

  const ppx = pxPerCmX();
  const ppy = pxPerCmY();

  state.objects.forEach(o=>{
    const cx = cmToPxX(o.x);
    const cy = cmToPxY(o.y);
    const wCm = o.w || 0;
    const hCm = o.h || 0;

    const w  = wCm * ppx;
    const h  = hCm * ppy;
    const ang= (o.angle || 0) * Math.PI/180;
    const color = o.color || '#888888';
    const label = o.label || o.type || '';

    const isCircle = o.shape === 'circle';

    ctx.save();
    ctx.translate(cx, cy);
    ctx.rotate(-ang);

    // Подсветка выделенного объекта
    const isSelected = state.selectedObjectId && o.id === state.selectedObjectId;

    // Тело объекта
    if(isCircle){
      const rPx = (wCm/2) * ppx; // диаметр = w, радиус = w/2
      ctx.beginPath();
      ctx.arc(0,0, rPx, 0, Math.PI*2);
      ctx.fillStyle = color + '33';
      ctx.fill();
      ctx.lineWidth = isSelected ? 3 : 2;
      ctx.strokeStyle = isSelected ? '#ff6b6b' : color;
      ctx.stroke();

      // Диаметр
      ctx.beginPath();
      ctx.moveTo(-rPx,0);
      ctx.lineTo( rPx,0);
      ctx.setLineDash([4,4]);
      ctx.strokeStyle = '#222';
      ctx.lineWidth = 1;
      ctx.stroke();
      ctx.setLineDash([]);

      ctx.fillStyle = '#111';
      ctx.fillText(`D=${round(wCm,1)} см`, 0, -rPx-8);
    } else {
      // Прямоугольник
      ctx.beginPath();
      ctx.rect(-w/2, -h/2, w, h);
      ctx.fillStyle = color + '33';
      ctx.fill();
      ctx.lineWidth = isSelected ? 3 : 2;
      ctx.strokeStyle = isSelected ? '#ff6b6b' : color;
      ctx.stroke();

      // Размеры по краям (в см)
      ctx.strokeStyle = '#222';
      ctx.lineWidth = 1;
      ctx.setLineDash([4,4]);

      // Горизонталь (ширина)
      ctx.beginPath();
      ctx.moveTo(-w/2, -h/2 - 6);
      ctx.lineTo( w/2, -h/2 - 6);
      ctx.stroke();

      // Вертикаль (высота)
      ctx.beginPath();
      ctx.moveTo(w/2 + 6, -h/2);
      ctx.lineTo(w/2 + 6,  h/2);
      ctx.stroke();

      ctx.setLineDash([]);

      ctx.fillStyle = '#111';
      ctx.fillText(`${round(wCm,1)} см`, 0, -h/2 - 10);
      ctx.save();
      ctx.rotate(-Math.PI/2);
      ctx.fillText(`${round(hCm,1)} см`, 0, w/2 + 14);
      ctx.restore();
    }

    // Подпись объекта (тип/название)
    if(label){
      ctx.fillStyle = '#0b2b4a';
      ctx.font = '12px sans-serif';
      ctx.fillText(label, 0, isCircle ? 16 : 18);
    }
// === РУЧКА ВРАЩЕНИЯ ОБЪЕКТА ===
{
  const offset = isCircle ? (w/2 + 20) : (h/2 + 20);

  const rx = 0;
  const ry = -offset;

  ctx.beginPath();
  ctx.arc(rx, ry, 7, 0, Math.PI * 2);
  ctx.fillStyle = '#ffffff';
  ctx.fill();
  ctx.lineWidth = 2;
  ctx.strokeStyle = isSelected ? '#ff6b6b' : '#0b5fff';
  ctx.stroke();

  // тонкая линия от центра
  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(rx, ry);
  ctx.strokeStyle = '#94a3ff';
  ctx.lineWidth = 1.5;
  ctx.stroke();
}

    // Угол объекта (если не нулевой) — над объектом
    const angleDeg = Math.round((o.angle || 0) % 360);
    if (angleDeg !== 0){
      ctx.save();
      ctx.fillStyle = '#d00';
      ctx.font = '13px sans-serif';
      ctx.textAlign = 'center';
      const offset = isCircle ? (w/2 + 28) : (h/2 + 28);
      ctx.fillText(`${angleDeg}°`, 0, -offset);
      ctx.restore();
    }

    ctx.restore();
  });

  ctx.restore();
}

/* Точки маршрута и ориентация в точках */
function drawPoints(){
  // ломаная по точкам
  if(state.points.length>=2){
    ctx.beginPath();
    state.points.forEach((p,i)=>{
      const x=cmToPxX(p.x), y=cmToPxY(p.y);
      if(i===0) ctx.moveTo(x,y); else ctx.lineTo(x,y);
    });
    ctx.strokeStyle='#0b5fff'; ctx.lineWidth=2; ctx.stroke();
  }

  // сами точки и ручка вращения
  state.points.forEach((p,i)=>{
    const x=cmToPxX(p.x), y=cmToPxY(p.y);

    // фон
    ctx.beginPath(); ctx.arc(x,y,9,0,Math.PI*2);
    ctx.fillStyle='rgba(0,0,0,0.06)'; ctx.fill();

    // внутренняя точка
    ctx.beginPath(); ctx.arc(x,y,6,0,Math.PI*2);
    ctx.fillStyle = (state.selected===i)?'#ff6b6b':'#fff';
    ctx.fill();
    ctx.strokeStyle = (state.selected===i)?'#ff6b6b':'#0b5fff';
    ctx.lineWidth=2; ctx.stroke();

    // индекс
    ctx.fillStyle='#0b2b4a'; ctx.font='11px sans-serif';
    ctx.fillText((i+1), x+8, y-8);

    // направление в точке
    const dispAngDeg = applyZeroHeading(p.heading || 0);
    const ang = dispAngDeg * Math.PI / 180;
    const len = 28;
    const hx = x + Math.cos(ang) * len;
    const hy = y - Math.sin(ang) * len; // минус, т.к. Y экрана вниз
    drawArrow(x,y,hx,hy,6);

    // ручка вращения
    const rx = x + Math.cos(ang) * (len+18);
    const ry = y - Math.sin(ang) * (len+18);
    ctx.beginPath(); ctx.arc(rx,ry,6,0,Math.PI*2);
    ctx.fillStyle='#fff'; ctx.fill(); ctx.strokeStyle='#94a3ff'; ctx.stroke();
  });
}

/* Прямоугольник робота (центр = текущая позиция) */
function drawRobot(){
  const cx=cmToPxX(robot.x);
  const cy=cmToPxY(robot.y);
  const w=robot.width*pxPerCmX();
  const h=robot.height*pxPerCmY();

  const dispAngDeg = applyZeroHeading(robot.heading || 0);
  const a = dispAngDeg * Math.PI/180;

  ctx.save();
  ctx.translate(cx,cy);
  ctx.rotate(-a); // чтобы "вперёд" совпадал со стрелкой

  // корпус
  ctx.fillStyle='rgba(11,95,255,0.12)';
  ctx.strokeStyle='#0b5fff'; ctx.lineWidth=2;
  ctx.beginPath(); ctx.rect(-w/2,-h/2,w,h); ctx.fill(); ctx.stroke();

  // индикатор «нос»
  ctx.beginPath(); ctx.moveTo(0,0); ctx.lineTo(w/2,0); ctx.stroke();

  ctx.restore();
}

/* === Фоновое изображение поля (опционально) === */
let bgImage = new Image();
bgImage.src = '../assets/map.png'; // путь к твоей карте (можно .jpg, .png)

function drawBackground() {
  if (bgImage.complete && bgImage.naturalWidth > 0) {
    ctx.drawImage(bgImage, 0, 0, canvas.width, canvas.height);
  } else {
    bgImage.onload = () => ctx.drawImage(bgImage, 0, 0, canvas.width, canvas.height);
  }
}

/* Линейка (измерение расстояния) */
function drawMeasure(){
  const m = state.measure;
  const pts = m.points;
  if (!m.enabled || !pts || pts.length === 0) return;

  ctx.save();
  ctx.fillStyle = '#d00';
  ctx.strokeStyle = '#d00';
  ctx.lineWidth = 2;

  // рисуем точки
  pts.forEach(p => {
    const x = cmToPxX(p.x);
    const y = cmToPxY(p.y);
    ctx.beginPath();
    ctx.arc(x, y, 6, 0, Math.PI * 2);
    ctx.fill();
  });

  // рисуем линию
  if (pts.length >= 2) {
    const a = pts[0], b = pts[1];
    const x1 = cmToPxX(a.x), y1 = cmToPxY(a.y);
    const x2 = cmToPxX(b.x), y2 = cmToPxY(b.y);

    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();

    const dist = Math.hypot(a.x - b.x, a.y - b.y).toFixed(1);

    ctx.font = '14px sans-serif';
    ctx.fillText(`${dist} см`, (x1 + x2) / 2, (y1 + y2) / 2 - 8);
  }

  ctx.restore();
}

/* Полный рендер */
function render(){
  clear();
  drawBackground();
  drawGrid();
  drawXRuler();
  drawYRuler();
  drawObjects();
  drawPoints();
  drawRobot();
  drawMeasure();
}

App.render = render;
