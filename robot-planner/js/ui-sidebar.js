/* ui-sidebar.js
   Левая панель: ИНСТРУМЕНТЫ, ОБЪЕКТЫ/ЛИНЕЙКА, СПИСОК точек,
   ВЫБРАННАЯ точка, СЦЕНАРИИ, ЛОГ.
*/
window.App = window.App || {};
const state    = App.state;
const registry = App.actionRegistry || {};

/* =======================
   ИНСТРУМЕНТЫ
   ======================= */
const modeAdd  = document.getElementById('modeAdd');
const modeMove = document.getElementById('modeMove');
const modeRot  = document.getElementById('modeRot');
const modeActs = document.getElementById('modeActs');
const deletePoint = document.getElementById('deletePoint');

const assistStraight = document.getElementById('assistStraight');
const assistTankSnap = document.getElementById('assistTankSnap');
const angleStepSel   = document.getElementById('angleStep');
const angleSnapChk   = document.getElementById('angleSnap');

const canvas = document.getElementById('fieldCanvas');

/* ОБЪЕКТЫ / ЛИНЕЙКА */
const modeObjects     = document.getElementById('modeObjects');
const modeMeasure     = document.getElementById('modeMeasure');
const deleteObjectBtn = document.getElementById('deleteObject');

const objTypeSel   = document.getElementById('objType');
const objWInput    = document.getElementById('objW');
const objHInput    = document.getElementById('objH');
const objColorInput= document.getElementById('objColor');
const objLabelInput= document.getElementById('objLabel');
const objPropsBox = document.getElementById('objectProps');
const objXInput   = document.getElementById('objX');
const objYInput   = document.getElementById('objY');
const objPWInput  = document.getElementById('objPW');
const objPHInput  = document.getElementById('objPH');
const objAngleInp = document.getElementById('objAngle');
const applyObjProps = document.getElementById('applyObjProps');

/* Курсоры по режимам — ставим CSS-класс на canvas */
function updateCanvasCursor(){
  canvas.className = canvas.className.replace(/\bmode-\w+\b/g,'');
  canvas.classList.add('mode-'+state.mode);
}
App.updateCanvasCursor = updateCanvasCursor;

function syncObjectProps() {
  const id = state.selectedObjectId;
  if (!id) {
    objPropsBox.style.display = 'none';
    return;
  }

  const o = (state.objects||[]).find(o => o.id === id);
  if (!o) {
    objPropsBox.style.display = 'none';
    return;
  }

  objPropsBox.style.display = 'block';

  objXInput.value = o.x.toFixed(1);
  objYInput.value = o.y.toFixed(1);
  objPWInput.value = o.w.toFixed(1);
  objPHInput.value = o.h.toFixed(1);
  objAngleInp.value = Math.round(o.angle || 0);
}


/* Переключение режимов + подсветка активной кнопки */
function setMode(m){
  state.mode = m;
  modeAdd.classList.toggle('ghost', m!=='add');
  modeMove.classList.toggle('ghost', m!=='move');
  modeRot.classList.toggle('ghost',  m!=='rotate');
  modeActs.classList.toggle('ghost', m!=='actions');

  if(modeObjects) modeObjects.classList.toggle('ghost', m!=='objects');
  if(modeMeasure) modeMeasure.classList.toggle('ghost', m!=='measure');

  // линейка
  if(m==='measure'){
    state.measure.enabled = true;
    state.measure.points = [];
  } else {
    state.measure.enabled = false;
    state.measure.points = [];
  }

  updateCanvasCursor();
}
App.setMode = setMode;
applyObjProps.onclick = () => {
  if (!state.selectedObjectId) return;
  const o = state.objects.find(o => o.id === state.selectedObjectId);
  if (!o) return;

  o.x = Number(objXInput.value)||0;
  o.y = Number(objYInput.value)||0;
  o.w = Number(objPWInput.value)||0;
  o.h = Number(objPHInput.value)||0;
  o.angle = Number(objAngleInp.value)||0;

  App.render();
  syncObjectProps();
};

/* Состояние ассистентов → UI */
function syncAssistUI(){
  assistStraight.classList.toggle('ghost', !state.assistStraight);
  assistStraight.textContent = 'Прямая: ' + (state.assistStraight?'вкл':'выкл') + ' (L)';
  assistTankSnap.classList.toggle('ghost', !state.assistTankSnap);
  assistTankSnap.textContent = 'Привязка 0/90°: ' + (state.assistTankSnap?'вкл':'выкл') + ' (K)';
  angleStepSel.value = String(state.angleSnapStep);
  angleSnapChk.checked = !!state.angleSnap;
}
App.syncAssistUI = syncAssistUI;

/* Обработчики кнопок инструментов */
modeAdd.onclick  = ()=> setMode('add');
modeMove.onclick = ()=> setMode('move');
modeRot.onclick  = ()=> setMode('rotate');
modeActs.onclick = ()=> setMode('actions');

deletePoint.onclick = ()=>{
  if(state.selected!==null){
    state.points.splice(state.selected,1);
    state.selected=null;
    renderLists();
    App.render();
  }
};

/* Обработчики ассистентов */
assistStraight.onclick = ()=>{ state.assistStraight=!state.assistStraight; syncAssistUI(); };
assistTankSnap.onclick = ()=>{ state.assistTankSnap=!state.assistTankSnap; syncAssistUI(); };
angleStepSel.onchange  = ()=>{ state.angleSnapStep = Number(angleStepSel.value)||5; };
angleSnapChk.onchange  = ()=>{ state.angleSnap = !!angleSnapChk.checked; };

/* ОБЪЕКТЫ */
if(modeObjects){
  modeObjects.onclick = ()=> setMode('objects');
}
if(modeMeasure){
  modeMeasure.onclick = ()=> setMode('measure');
}

if(deleteObjectBtn){
  deleteObjectBtn.onclick = ()=>{
    if(state.selectedObjectId){
      state.objects = (state.objects||[]).filter(o=>o.id!==state.selectedObjectId);
      state.selectedObjectId = null;
      App.render();
      syncObjectUI();
    }
  };
}

function syncObjectUI(){
  if(!objTypeSel || !objWInput || !objHInput || !objColorInput || !objLabelInput) return;
  const id = state.selectedObjectId;
  if(!id){
    // нет выделенного объекта — сбрасывать только label, остальные не трогаем
    objLabelInput.value = '';
    return;
  }
  const o = (state.objects||[]).find(o=>o.id===id);
  if(!o) return;
  objTypeSel.value    = o.type   || 'custom';
  objWInput.value     = o.w      ?? 30;
  objHInput.value     = o.h      ?? 5;
  objColorInput.value = o.color  || '#888888';
  objLabelInput.value = o.label  || '';
  syncObjectProps();

}
App.syncObjectUI = syncObjectUI;
syncObjectProps();


// Изменения из инпутов сразу улетают в объект
function applyObjectInputs(){
  if(!state.selectedObjectId) return;
  const o = (state.objects||[]).find(o=>o.id===state.selectedObjectId);
  if(!o) return;
  if(objTypeSel)   o.type  = objTypeSel.value;
  if(objWInput)    o.w     = Number(objWInput.value)||0;
  if(objHInput)    o.h     = Number(objHInput.value)||0;
  if(objColorInput)o.color = objColorInput.value;
  if(objLabelInput)o.label = objLabelInput.value;
  // форма: цилиндр = круг
  o.shape = (o.type === 'cylinder') ? 'circle' : (o.shape || 'rect');
  App.render();
}
if(objTypeSel)   objTypeSel.onchange   = applyObjectInputs;
if(objWInput)    objWInput.onchange    = applyObjectInputs;
if(objHInput)    objHInput.onchange    = applyObjectInputs;
if(objColorInput)objColorInput.onchange= applyObjectInputs;
if(objLabelInput)objLabelInput.onchange= applyObjectInputs;

/* =======================
   СПИСОК ТОЧЕК / ВЫБРАННАЯ
   ======================= */
const pointsList = document.getElementById('pointsList');
const selX   = document.getElementById('selX');
const selY   = document.getElementById('selY');
const selHead= document.getElementById('selHead');
const applyPoint = document.getElementById('applyPoint');
const deselectBtn= document.getElementById('deselect');

function renderPointsList(){
  pointsList.innerHTML='';
  state.points.forEach((p,i)=>{
    const card=document.createElement('div'); card.className='point-card';
    const head=document.createElement('div'); head.className='row';

    const left=document.createElement('div');
    left.innerHTML = `<b>#${i+1}</b><span class="tag">${(p.actions?.length||0)} act</span>
      <div class="coords">x:${p.x.toFixed(1)} y:${p.y.toFixed(1)} θ:${Math.round(p.heading ?? 0)}</div>`;

    const right=document.createElement('div');
    const bSel=document.createElement('button'); bSel.className='small'; bSel.textContent='Выбрать';
    bSel.onclick=()=>{ 
      state.selected=i; 
      state.selectedObjectId=null; 
      syncSelectedInputs(); 
      renderActionsList(); 
      App.render(); 
    };
    const bDel=document.createElement('button'); bDel.className='small ghost'; bDel.textContent='Удалить';
    bDel.onclick=()=>{ 
      state.points.splice(i,1); 
      if(state.selected===i) state.selected=null; 
      renderLists(); 
      App.render(); 
    };

    right.appendChild(bSel); right.appendChild(bDel);
    head.appendChild(left); head.appendChild(right);
    card.appendChild(head);
    pointsList.appendChild(card);
  });
}

function syncSelectedInputs(){
  if(state.selected===null){
    selX.value=''; selY.value=''; selHead.value='';
  } else {
    const p=state.points[state.selected];
    selX.value=p.x; selY.value=p.y; selHead.value=p.heading??0;
  }
}

/* Квантование угла (если включено) */
function snapAngleIfNeeded(deg){
  if(!state.angleSnap) return ((deg%360)+360)%360;
  const step = Math.max(1, Number(state.angleSnapStep)||5);
  const a = Math.round(deg/step)*step;
  return ((a%360)+360)%360;
}

/* Применить изменения к выбранной точке */
applyPoint.onclick = ()=>{
  if(state.selected===null) return;
  const p=state.points[state.selected];
  p.x = Math.max(0, Math.min(state.fieldWcm, Number(selX.value)||0));
  p.y = Math.max(0, Math.min(state.fieldHcm, Number(selY.value)||0));
  p.heading = snapAngleIfNeeded(Number(selHead.value)||0);
  selHead.value = p.heading;
  App.render();
};

deselectBtn.onclick = ()=>{
  state.selected=null; 
  state.selectedObjectId=null;
  renderLists(); 
  App.render(); 
};

/* =======================
   СЦЕНАРИИ
   ======================= */
const actionName = document.getElementById('actionName');
const actionArgs = document.getElementById('actionArgsForm');
const addAction  = document.getElementById('addAction');
const actionPar  = document.getElementById('actionParallel');
const actionsList= document.getElementById('actionsList');

function populateActionSelect(){
  actionName.innerHTML='';
  Object.keys(registry).forEach(k=>{
    const o=document.createElement('option');
    o.value=k; o.textContent=`${registry[k].label} (${k})`;
    actionName.appendChild(o);
  });
  renderActionArgsForm();
}

function renderActionArgsForm(){
  const name=actionName.value; actionArgs.innerHTML=''; if(!name) return;
  const info=registry[name];
  (info.args||[]).forEach(arg=>{
    const wrap=document.createElement('div'); wrap.style.marginBottom='6px';
    const lab=document.createElement('label'); lab.textContent=arg.label||arg.name;
    let input;
    if(arg.type==='select'){
      input=document.createElement('select');
      (arg.options||[]).forEach(o=>{
        const op=document.createElement('option');
        op.value=o.value; op.textContent=o.label; input.appendChild(op);
      });
      input.value = arg.default ?? '';
    } else {
      input=document.createElement('input');
      input.type = arg.type==='number' ? 'number' : 'text';
      input.value= arg.default ?? '';
    }
    input.dataset.argName=arg.name;
    wrap.appendChild(lab); wrap.appendChild(input);
    actionArgs.appendChild(wrap);
  });
}

actionName.onchange = renderActionArgsForm;

function renderActionsList(){
  actionsList.innerHTML='';
  if(state.selected===null){ actionsList.innerHTML='<div class="muted">Точка не выбрана</div>'; return; }
  const list=state.points[state.selected].actions||[];
  if(list.length===0){ actionsList.innerHTML='<div class="muted">Нет действий</div>'; return; }

  list.forEach((a,idx)=>{
    const row=document.createElement('div'); row.className='row';
    row.style.border='1px solid #eef2ff'; row.style.borderRadius='10px';
    row.style.marginBottom='6px'; row.style.padding='8px';

    const left=document.createElement('div');
    left.innerHTML=`<b>${registry[a.name]?.label||a.name}</b> <span class="muted">(${a.name})</span>
      <div class="muted" style="font-size:12px;margin-top:4px">
      ${Object.entries(a.args||{}).map(([k,v])=>`${k}: ${v}`).join(', ')||'без арг.'}
      </div>
      <div class="muted" style="font-size:12px">Параллельно: ${a.parallel?'да':'нет'}</div>`;

    const right=document.createElement('div');
    const up=document.createElement('button'); up.className='small'; up.textContent='▲';
    up.onclick=()=>{ 
      if(idx>0){ 
        let arr=state.points[state.selected].actions; 
        [arr[idx-1],arr[idx]]=[arr[idx],arr[idx-1]]; 
        renderActionsList(); renderPointsList(); 
      } 
    };
    const dn=document.createElement('button'); dn.className='small'; dn.textContent='▼';
    dn.onclick=()=>{ 
      let arr=state.points[state.selected].actions; 
      if(idx<arr.length-1){ 
        [arr[idx+1],arr[idx]]=[arr[idx],arr[idx+1]]; 
        renderActionsList(); renderPointsList(); 
      } 
    };
    const del=document.createElement('button'); del.className='small ghost'; del.textContent='Удалить';
    del.onclick=()=>{ state.points[state.selected].actions.splice(idx,1); renderActionsList(); renderPointsList(); };

    right.appendChild(up); right.appendChild(dn); right.appendChild(del);
    row.appendChild(left); row.appendChild(right);
    actionsList.appendChild(row);
  });
}

addAction.onclick = ()=>{
  if(state.selected===null){ alert('Сначала выберите точку'); return; }
  const def=registry[actionName.value]; const args={};
  (def.args||[]).forEach(arg=>{
    const el=actionArgs.querySelector(`[data-arg-name="${arg.name}"]`);
    let v=el?el.value:''; if(arg.type==='number') v=Number(v); args[arg.name]=v;
  });
  const act = {name:actionName.value, args, parallel: !!actionPar.checked};
  state.points[state.selected].actions = state.points[state.selected].actions||[];
  state.points[state.selected].actions.push(act);
  renderActionsList(); renderPointsList();
};

function renderLists(){ renderPointsList(); syncSelectedInputs(); renderActionsList(); }
App.renderLists = renderLists;

/* Инициализация панели */
(function init(){
  angleStepSel.value = String(state.angleSnapStep);
  angleSnapChk.checked = !!state.angleSnap;
  setMode('add'); 
  syncAssistUI();
  populateActionSelect();
  renderLists();
})();
