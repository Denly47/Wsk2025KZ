/* ui-canvas.js
   Работа с холстом: точки, объекты, линейка, hotkeys.
   Режимы: add | move | rotate | actions | objects | measure
*/
window.App = window.App || {};
const state = App.state;
const robot = App.robot;

const canvas = document.getElementById('fieldCanvas');
const ctx = canvas.getContext('2d');

/* Конвертеры см ↔ px */
function pxPerCmX(){ return state.canvasWpx / state.fieldWcm; }
function pxPerCmY(){ return state.canvasHpx / state.fieldHcm; }
function cmToPxX(x){ return x * pxPerCmX(); }
function cmToPxY(y){ return state.canvasHpx - y * pxPerCmY(); }
function pxToCmX(px){ return px / pxPerCmX(); }
function pxToCmY(px){ return (state.canvasHpx - px) / pxPerCmY(); }
function distance(a,b){ return Math.hypot(a.x-b.x, a.y-b.y); }
function clamp(v,a,b){ return Math.max(a, Math.min(b, v)); }

function normDeg(a){
  a %= 360;
  if (a < 0) a += 360;
  return a;
}

/* heading → экранный угол (как в drawing.js) */
function applyZeroHeading(h){
  h = normDeg(h);
  switch (robot.zero){
    case 'down':  return normDeg(h + 90);   // 0° вниз
    case 'up':    return normDeg(h + 270);  // 0° вверх
    case 'right': return normDeg(h + 0);    // 0° вправо
    case 'left':  return normDeg(h + 180);  // 0° влево
    default:      return h;
  }
}
App.applyZeroHeading = applyZeroHeading;

/* обратное: экранный угол → heading в планировщике */
function invertZeroHeading(displayDeg){
  const d = normDeg(displayDeg);
  switch (robot.zero){
    case 'down':  return normDeg(d - 90);
    case 'up':    return normDeg(d - 270);
    case 'right': return d;
    case 'left':  return normDeg(d - 180);
    default:      return d;
  }
}

/* Для синхронизации UI из хоткеев */
function callSyncAssist(){ if(window.App && App.syncAssistUI) App.syncAssistUI(); }
function callSyncObjectUI(){ if(window.App && App.syncObjectUI) App.syncObjectUI(); }

/* Квантование угла (0..360) */
function snapAngle(deg){
  let a = normDeg(deg);
  if(!state.angleSnap) return a;
  const step = Math.max(1, Number(state.angleSnapStep)||5);
  a = Math.round(a/step)*step;
  return normDeg(a);
}

/* hit-test для точки и ручки вращения */
function findPointHit(px,py){
  for(let i=0;i<state.points.length;i++){
    const p=state.points[i]; const x=cmToPxX(p.x), y=cmToPxY(p.y);
    if(distance({x,y},{x:px,y:py})<=9) return {type:'point',i};

    // ручка вращения — ориентируемся по экранному углу
    const dispAngDeg = applyZeroHeading(p.heading || 0);
    const ang = dispAngDeg * Math.PI/180;
    const rx = x + Math.cos(ang)*(28+18);
    const ry = y - Math.sin(ang)*(28+18);
    if(distance({x:rx,y:ry},{x:px,y:py})<=8) return {type:'rotate',i};
  }
  return null;
}

/* hit-test для объекта: тело + ручка вращения */
function findObjectHit(px, py){
  if(!state.objects) return null;
  for(const o of state.objects){
    const cx = cmToPxX(o.x);
    const cy = cmToPxY(o.y);
    const w  = (o.w || 0) * pxPerCmX();
    const h  = (o.h || 0) * pxPerCmY();
    const ang= (o.angle || 0) * Math.PI/180;

    // проверка ручки вращения (кружок над объектом)
    const handleX = cx;
    const handleY = cy - (h/2 + 20);
    if(distance({x:handleX,y:handleY},{x:px,y:py}) <= 8){
      return { type:'object-rotate', object:o };
    }

    // преобразуем в локальные координаты объекта
    const dx = px - cx;
    const dy = py - cy;
    const rx =  dx * Math.cos(ang) + dy * Math.sin(ang);
    const ry = -dx * Math.sin(ang) + dy * Math.cos(ang);

    const isCircle = o.shape === 'circle';
    if(isCircle){
      const r = (o.w || 0)/2 * pxPerCmX();
      if(rx*rx + ry*ry <= r*r){
        return { type:'object', object:o };
      }
    } else {
      if(rx >= -w/2 && rx <= w/2 && ry >= -h/2 && ry <= h/2){
        return { type:'object', object:o };
      }
    }
  }
  return null;
}

/* Поиск точки линейки рядом с мышью */
function findMeasurePoint(px, py) {
  const pts = state.measure.points || [];
  for (let i = 0; i < pts.length; i++) {
    const ax = cmToPxX(pts[i].x);
    const ay = cmToPxY(pts[i].y);
    if (distance({x: ax, y: ay}, {x: px, y: py}) <= 10) {
      return i;
    }
  }
  return -1;
}

let dragging=false, dragType=null, dragOffset={x:0,y:0};
let dragObject = null;
let dragMeasureIndex = null;

function getMousePos(evt){
  const r=canvas.getBoundingClientRect();
  return {x:evt.clientX - r.left, y:evt.clientY - r.top};
}

/* Локальный setMode, который вызывает App.setMode из sidebar */
function setMode(m){
  if (window.App && App.setMode) {
    App.setMode(m);
  } else {
    state.mode = m;
    App.updateCanvasCursor && App.updateCanvasCursor();
  }
}

/* === Мышь: mousedown === */
canvas.addEventListener('mousedown',(e)=>{
  const m=getMousePos(e);

  /* --- Режим линейки --- */
  if (state.mode === 'measure') {
    const idx = findMeasurePoint(m.x, m.y);

    // потянуть существующую точку
    if (idx !== -1) {
      dragging = true;
      dragType = 'measure';
      dragMeasureIndex = idx;
      return;
    }

    // добавить новую точку
    const pts = state.measure.points;
    if (pts.length >= 2) pts.length = 0;
    pts.push({
      x: pxToCmX(m.x),
      y: pxToCmY(m.y)
    });
    App.render();
    return;
  }

  /* --- Режим объектов --- */
  if(state.mode==='objects'){
    const hitObj = findObjectHit(m.x, m.y);
    if(hitObj){
   

      state.selectedObjectId = hitObj.object.id;
      state.selected = null; // не выделяем точку
      callSyncObjectUI();
         if (window.App && App.syncObjectProps) App.syncObjectProps();
      if(hitObj.type === 'object'){
        dragging = true;
        dragType = 'object-move';
        dragObject = hitObj.object;
        const px = cmToPxX(dragObject.x);
        const py = cmToPxY(dragObject.y);
        dragOffset = { x: m.x - px, y: m.y - py };
        canvas.classList.add('grabbing');
      } else if(hitObj.type === 'object-rotate'){
        dragging = true;
        dragType = 'object-rotate';
        dragObject = hitObj.object;
      }
      App.render();
      return;
    }

    // клик по пустому месту → создать новый объект
    const xcm = Math.round(pxToCmX(m.x)*10)/10;
    const ycm = Math.round(pxToCmY(m.y)*10)/10;

    const typeEl  = document.getElementById('objType');
    const wEl     = document.getElementById('objW');
    const hEl     = document.getElementById('objH');
    const colorEl = document.getElementById('objColor');
    const labelEl = document.getElementById('objLabel');

    const type  = typeEl ? typeEl.value : 'custom';
    const w     = wEl ? Number(wEl.value)||30 : 30;
    const h     = hEl ? Number(hEl.value)||5  : 5;
    const color = colorEl ? colorEl.value : '#888888';
    const label = labelEl ? labelEl.value : '';

    const shape = (type === 'cylinder') ? 'circle' : 'rect';

    const obj = {
      id: Date.now() + Math.random(),
      type,
      shape,
      x: xcm,
      y: ycm,
      w,
      h,
      angle: 0,
      color,
      label
    };

    state.objects = state.objects || [];
    state.objects.push(obj);
    state.selectedObjectId = obj.id;
    state.selected = null;
    callSyncObjectUI();
    App.render();
    return;
  }

  /* --- Режимы работы с точками --- */
  const hit=findPointHit(m.x,m.y);

  if(state.mode==='add'){
    if(!hit){
      const prev = state.points[state.points.length-1];
      let x = Math.round(pxToCmX(m.x)*10)/10;
      let y = Math.round(pxToCmY(m.y)*10)/10;
      const shift = e.shiftKey;

      // Прямая: фиксируем X или Y к предыдущей точке
      if(prev && (state.assistStraight || shift)){
        if(Math.abs(x - prev.x) < Math.abs(y - prev.y)) x=prev.x; else y=prev.y;
      }

      // Базовый heading = предыдущий (если есть), квантуем
      let heading = snapAngle(prev ? prev.heading : 0);

      // Для Tank: прилипание к 0/90/180/270 — корректируем координаты и угол
      if(prev && state.assistTankSnap && (robot.drive==='Tank')){
        const angDeg = normDeg(Math.atan2((y - prev.y),(x - prev.x))*180/Math.PI);
        const snaps=[0,90,180,270];
        let best=snaps[0], diff=999;
        snaps.forEach(s=>{
          const d=Math.abs(((angDeg - s + 540)%360)-180);
          if(d<diff){ diff=d; best=s; }
        });
        if(diff <= 8){
          if(best===0||best===180) y=prev.y; else x=prev.x;
          heading = best;
        }
      }

      state.points.push({x, y, heading, actions:[]});
      state.selected=state.points.length-1;
      state.selectedObjectId = null;

      // Если это первая точка — сразу ставим туда робота
      if(state.points.length === 1){
        robot.x = x;
        robot.y = y;
        robot.heading = heading;
      }

      App.render();
      App.renderLists && App.renderLists();

    } else {
      state.selected=hit.i;
      state.selectedObjectId = null;
      App.render();
      App.renderLists && App.renderLists();
    }
  }
  else if(state.mode==='move'){
    if(hit && hit.type==='point'){
      dragging=true; state.selected=hit.i;
      state.selectedObjectId = null;
      const p=state.points[hit.i]; const px=cmToPxX(p.x), py=cmToPxY(p.y);
      dragOffset={x:m.x-px, y:m.y-py}; canvas.classList.add('grabbing'); App.render();
      App.renderLists && App.renderLists();
    }else{
      state.selected=null; App.render(); App.renderLists && App.renderLists();
    }
  }
  else if(state.mode==='rotate'){
    if(hit && hit.type==='rotate'){
      dragging=true; dragType='rotate'; state.selected=hit.i; state.selectedObjectId=null;
      App.render(); App.renderLists && App.renderLists();
    }
  }
  else if(state.mode==='actions'){
    if(hit){ state.selected=hit.i; state.selectedObjectId=null; App.render(); App.renderLists && App.renderLists(); }
  }
});

/* === Мышь: mousemove === */
canvas.addEventListener('mousemove',(e)=>{
  if(!dragging) return;
  const m=getMousePos(e);

  // Перетаскивание точки линейки
  if (dragType === 'measure' && dragMeasureIndex !== null) {
    state.measure.points[dragMeasureIndex].x = pxToCmX(m.x);
    state.measure.points[dragMeasureIndex].y = pxToCmY(m.y);
    App.render();
    return;
  }

  // Перемещение объекта
// Перемещение объекта
if(dragType==='object-move' && dragObject){
    let nx = pxToCmX(m.x - dragOffset.x);
    let ny = pxToCmY(m.y - dragOffset.y);
    nx = Math.round(nx*10)/10;
    ny = Math.round(ny*10)/10;

    dragObject.x = clamp(nx,0,state.fieldWcm);
    dragObject.y = clamp(ny,0,state.fieldHcm);

    App.render();

    // 🔥 ВАЖНО: обновить панель свойств
    if (window.App && App.syncObjectUI) App.syncObjectUI();
    if (window.App && App.syncObjectProps) App.syncObjectProps();

    return;
}


  // Вращение объекта
// Вращение объекта
if (dragType === 'object-rotate' && dragObject) {
    const cx = cmToPxX(dragObject.x);
    const cy = cmToPxY(dragObject.y);

    const displayDeg = -Math.atan2((m.y - cy), (m.x - cx)) * 180 / Math.PI;
    dragObject.angle = snapAngle(displayDeg);

    App.render();

    // 🔥 Обновить панель свойств
    if (window.App && App.syncObjectUI) App.syncObjectUI();
    if (window.App && App.syncObjectProps) App.syncObjectProps();

    return;
}

  // Перемещение точки
  if(state.mode==='move' && state.selected!==null){
    let nx = pxToCmX(m.x - dragOffset.x);
    let ny = pxToCmY(m.y - dragOffset.y);
    nx = Math.round(nx*10)/10; ny = Math.round(ny*10)/10;

    const prev=state.points[state.selected-1];
    const shift=e.shiftKey;
    if(prev && (state.assistStraight || shift)){
      if(Math.abs(nx - prev.x) < Math.abs(ny - prev.y)) nx=prev.x; else ny=prev.y;
    }

    state.points[state.selected].x = clamp(nx,0,state.fieldWcm);
    state.points[state.selected].y = clamp(ny,0,state.fieldHcm);

    // Если это первая точка — двигаем робота вместе с ней
    if (state.selected === 0) {
      robot.x = state.points[0].x;
      robot.y = state.points[0].y;
    }

    App.render();
    App.renderLists && App.renderLists();
  }
  // Вращение точки
  else if(state.mode==='rotate' && state.selected!==null && dragType==='rotate'){
    const p=state.points[state.selected];
    const cx=cmToPxX(p.x), cy=cmToPxY(p.y);
    // экранный угол (0° вправо, 90° вверх)
    const displayDeg = -Math.atan2((m.y - cy),(m.x - cx))*180/Math.PI;
    // переводим в heading планировщика с учётом ориентации 0°
    const rawHead = invertZeroHeading(displayDeg);
    p.heading = snapAngle(rawHead);

    if (state.selected === 0) {
      robot.heading = p.heading;
    }

    App.render();
    App.renderLists && App.renderLists();
  }
});

/* === Мышь: mouseup === */
window.addEventListener('mouseup',()=>{
  dragging=false; dragType=null; dragObject=null;
  dragMeasureIndex=null;
  canvas.classList.remove('grabbing');
});

/* === Горячие клавиши: режимы, ассистенты, симуляция, угол, скорость, объекты, линейка === */
document.addEventListener('keydown',(e)=>{
  const tag=(e.target&&e.target.tagName||'').toLowerCase();
  const inInput = tag==='input'||tag==='select'||tag==='textarea';

  // Delete — удалить выбранную точку или объект
  if(e.key==='Delete' && !inInput){
    if(state.selected!==null){
      state.points.splice(state.selected,1); state.selected=null;
      App.render(); App.renderLists && App.renderLists(); 
      return;
    }
    if(state.selectedObjectId){
      state.objects = (state.objects||[]).filter(o=>o.id!==state.selectedObjectId);
      state.selectedObjectId = null;
      App.render();
      callSyncObjectUI();
      return;
    }
  }

  // Ctrl/Cmd+S — сохранить JSON
  if((e.ctrlKey||e.metaKey) && e.key.toLowerCase()==='s'){
    e.preventDefault(); document.getElementById('saveJSON').click(); return;
  }

  if(inInput) return; // не перехватываем при наборе текста

  // Вращение выделенного объекта ←/→
  if (state.selectedObjectId) {
    const o = (state.objects||[]).find(o => o.id === state.selectedObjectId);
    if (o) {
      if (e.key === 'ArrowLeft') {
        o.angle = snapAngle((o.angle || 0) - 2);
        App.render();
        return;
      }
      if (e.key === 'ArrowRight') {
        o.angle = snapAngle((o.angle || 0) + 2);
        App.render();
        return;
      }
    }
  }

  const k=e.key.toLowerCase();

  // режимы точек/объектов/линейки
  if(k==='a'){ setMode('add'); return; }
  if(k==='m'){ setMode('move'); return; }
  if(k==='r'){ setMode('rotate'); return; }
  if(k==='s'){ setMode('actions'); return; }
  if(k==='o'){ setMode('objects'); return; }
  if(k==='d'){ setMode('measure'); App.render(); return; }

  // ассистенты
  if(k==='l'){ state.assistStraight=!state.assistStraight; callSyncAssist(); return; }
  if(k==='k'){ state.assistTankSnap=!state.assistTankSnap; callSyncAssist(); return; }

  // симуляция
  if(k==='p'){
    const st=document.getElementById('startSim');
    const sp=document.getElementById('stopSim');
    if(App.robot.running){ App.stopSim(); st.disabled=false; sp.disabled=true; }
    else { App.startSim(); st.disabled=true; sp.disabled=false; }
    return;
  }

  // точная подстройка угла у выбранной точки (±шаг)
  if((k==='q'||k==='e') && state.selected!==null){
    const step=Math.max(1, Number(state.angleSnapStep)||5);
    const delta=(k==='q')?-step:+step;
    const p=state.points[state.selected];
    p.heading = snapAngle((p.heading||0)+delta);
    App.render(); App.renderLists && App.renderLists(); return;
  }

  // скорость робота
  if(e.key==='+'||e.key==='='){
    App.robot.speed=Math.max(0,App.robot.speed+5);
    document.getElementById('robotSpeed').value=App.robot.speed; return;
  }
  if(e.key==='-'){
    App.robot.speed=Math.max(0,App.robot.speed-5);
    document.getElementById('robotSpeed').value=App.robot.speed; return;
  }
});
