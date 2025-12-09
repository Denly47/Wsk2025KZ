/* ui-topbar.js
   Компактный верхний бар: "Поле", "Робот/Сим", "Файлы".
   Никаких инструментов/ассистентов — они в левой панели.
*/
window.App = window.App || {};
const state = App.state;
const robot = App.robot;

const title = document.getElementById('title');
function updateTitle(){ title.textContent = `Поле: ${state.fieldWcm} × ${state.fieldHcm} см`; }

const fieldW  = document.getElementById('fieldW');
const fieldH  = document.getElementById('fieldH');
const canvasW = document.getElementById('canvasW');
const applySize = document.getElementById('applySize');
const clearAll  = document.getElementById('clearAll');

const robotW = document.getElementById('robotW');
const robotH = document.getElementById('robotH');
const driveMode  = document.getElementById('driveMode');
const robotSpeed = document.getElementById('robotSpeed');
const robotZero  = document.getElementById('robotZero');
const startSimBtn= document.getElementById('startSim');
const stopSimBtn = document.getElementById('stopSim');

const saveJSONBtn = document.getElementById('saveJSON');
const loadJSONBtn = document.getElementById('loadJSONBtn');
const loadJSONInp = document.getElementById('loadJSON');

const canvas = document.getElementById('fieldCanvas');

/* Применить размеры поля/холста */
applySize.onclick = ()=>{
  state.fieldWcm = Number(fieldW.value)||state.fieldWcm;
  state.fieldHcm = Number(fieldH.value)||state.fieldHcm;
  state.canvasWpx= Number(canvasW.value)||state.canvasWpx;
  state.canvasHpx= Math.max(200, Math.round(state.canvasWpx * (state.fieldHcm/state.fieldWcm)));
  canvas.width  = state.canvasWpx;
  canvas.height = state.canvasHpx;
  updateTitle();
  App.render();
  App.updateCanvasCursor && App.updateCanvasCursor();
};

/* Очистить точки */
clearAll.onclick = ()=>{
  if(confirm('Очистить все точки?')){
    state.points=[]; state.selected=null; App.render(); App.renderLists && App.renderLists();
  }
};

/* Параметры робота и симуляция */
driveMode.onchange = ()=>{ robot.drive = driveMode.value; };
robotW.onchange    = ()=>{ robot.width  = Number(robotW.value)||robot.width; App.render(); };
robotH.onchange    = ()=>{ robot.height = Number(robotH.value)||robot.height; App.render(); };
robotSpeed.onchange= ()=>{ robot.speed  = Number(robotSpeed.value)||robot.speed; };
robotZero.onchange = ()=>{ robot.zero   = robotZero.value || 'down'; App.render(); };

startSimBtn.onclick = ()=>{
  robot.running=false;
  App.startSim();
  startSimBtn.disabled=true; stopSimBtn.disabled=false;
};
stopSimBtn.onclick = ()=>{
  App.stopSim();
  startSimBtn.disabled=false; stopSimBtn.disabled=true;
};

/* Сохранить/загрузить маршрут */
function saveJSON(){
  const out = {
    field:{width_cm:state.fieldWcm,height_cm:state.fieldHcm},
    robot:{
      width_cm:robot.width,
      height_cm:robot.height,
      drive: robot.drive,
      speed: robot.speed,
      zero: robot.zero
    },
    path: state.points,
    objects: state.objects || []
  };
  const blob = new Blob([JSON.stringify(out,null,2)],{type:'application/json'});
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = 'route.json';
  a.click();
  App.log && App.log('Файл route.json сохранён');
}
saveJSONBtn.onclick  = ()=> saveJSON();
loadJSONBtn.onclick  = ()=> loadJSONInp.click();
loadJSONInp.onchange = (e)=>{
  const f=e.target.files[0]; if(!f) return;
  const r=new FileReader();
  r.onload=()=>{
    try{
      const obj=JSON.parse(r.result);
      state.fieldWcm = obj.field?.width_cm  ?? state.fieldWcm;
      state.fieldHcm = obj.field?.height_cm ?? state.fieldHcm;

      robot.width  = obj.robot?.width_cm  ?? robot.width;
      robot.height = obj.robot?.height_cm ?? robot.height;
      robot.drive  = obj.robot?.drive     ?? robot.drive;
      robot.speed  = obj.robot?.speed     ?? robot.speed;
      robot.zero   = obj.robot?.zero      ?? robot.zero;

      state.points = Array.isArray(obj.path) ? obj.path.map(p=>({
        x:+p.x||0, y:+p.y||0, heading:+p.heading||0, actions:p.actions||[]
      })) : [];

            state.objects = Array.isArray(obj.objects) ? obj.objects.map(o=>({
        id: o.id ?? (Date.now() + Math.random()),
        type: o.type || 'custom',
        shape: o.shape || (o.type === 'cylinder' ? 'circle' : 'rect'),
        x:+o.x||0,
        y:+o.y||0,
        w:+o.w||0,
        h:+o.h||0,
        angle:+(o.angle||0),
        color: o.color || '#888888',
        label: o.label || ''
      })) : [];


      fieldW.value   = state.fieldWcm;
      fieldH.value   = state.fieldHcm;
      canvasW.value  = state.canvasWpx;
      robotW.value   = robot.width;
      robotH.value   = robot.height;
      driveMode.value= robot.drive;
      robotSpeed.value=robot.speed;
      robotZero.value = robot.zero || 'down';

      applySize.click();
      App.renderLists && App.renderLists();
      App.log && App.log('Маршрут загружен');
    }catch(err){ alert('Ошибка JSON: '+err.message); }
  };
  r.readAsText(f);
};

/* === ВАЖНО: визуальный heading = yaw в Java === */
function toYaw(h) {
  return ((h % 360) + 360) % 360;   // нормализация
}

// расстояние и разложение по осям (мм)
function segmentToCommand(prev, cur) {
    const dx = (cur.x - prev.x) * 10;
    const dy = (cur.y - prev.y) * 10;
    if (Math.abs(dx) < 1 && Math.abs(dy) < 1) return null;

    // ВАЖНО: берём экранный heading (с учётом robot.zero)
    const screenH = App.applyZeroHeading(prev.heading || 0); 
    const h = screenH * Math.PI/180;

    // Перевод в координаты робота
    const dxR =  dx * Math.cos(h) + dy * Math.sin(h);   // вперёд/назад
    const dyR = -dx * Math.sin(h) + dy * Math.cos(h);   // вправо/влево

    const yaw = toYaw(prev.heading || 0);

    // Выбор команды
    if (Math.abs(dxR) >= Math.abs(dyR)) {
        const dist = Math.round(dxR);
        return `new DriveEnc(${dist}, ${dist}, 0.1, 0.1, ${yaw}, 0.1)`;
    } else {
        const dist = Math.round(dyR);
        return `new strafe(${dist}, 0.1, ${yaw}, 0.1)`;
    }
}


/* Инициализация значений в полях + экспорт Java */
(function init(){
  fieldW.value   = state.fieldWcm;
  fieldH.value   = state.fieldHcm;
  canvasW.value  = state.canvasWpx;
  robotW.value   = robot.width;
  robotH.value   = robot.height;
  driveMode.value= robot.drive;
  robotSpeed.value=robot.speed;
  robotZero.value = robot.zero || 'down';
  applySize.click();

  /* === Экспорт Java DriveMotor.java (heading = yaw 1:1) === */
  const exportJavaBtn = document.createElement('button');
  exportJavaBtn.textContent = 'Экспорт Java';
  exportJavaBtn.className = 'small ghost';
  saveJSONBtn.parentNode.appendChild(exportJavaBtn);

  exportJavaBtn.onclick = () => {
    if (!state.points || state.points.length === 0) {
      alert('Нет точек для экспорта');
      return;
    }

    const pts = state.points;
    const cmds = [];
    cmds.push('package frc.robot.commands.auto;');
    cmds.push('');
    cmds.push('public class DriveMotor extends AutoCommand {');
    cmds.push('    public DriveMotor() {');
    cmds.push('        super(');


    /* === 1. Поворот робота к heading первой точки === */
    const firstYaw = toYaw(pts[0].heading || 0);
    cmds.push(`            new TurnToAngle(${firstYaw}, 0.1),`);


    /* === 2. Все сегменты пути === */
    for (let i = 1; i < pts.length; i++) {
      const prev = pts[i - 1];
      const cur  = pts[i];

      const moveCmd = segmentToCommand(prev, cur);
      if (moveCmd) cmds.push(`            ${moveCmd},`);

      const prevH = toYaw(prev.heading || 0);
      const curH  = toYaw(cur.heading  || 0);

      if (Math.abs(prevH - curH) > 0.5) {
        cmds.push(`            new TurnToAngle(${curH}, 0.1),`);
      }

      (cur.actions || []).forEach(a => {
        let c = '';
        switch (a.name) {
          case 'sensor':
            c = `new Sensor(${a.args.targetRightX}, ${a.args.targetLeftX}, ${a.args.epsilonX}, ${a.args.targetLeftY}, ${a.args.targetRightY}, ${a.args.epsilonY}, ${a.args.targetYaw}, ${a.args.epsilonYaw}).withTimeout(${a.args.timeout})`;
            break;
          case 'sensorLidar':
            c = `new Sensor_Lidar(${a.args.targetRightX}, ${a.args.targetLeftX}, ${a.args.epsilonX}, ${a.args.targetFrontY}, ${a.args.targetBackY}, ${a.args.epsilonY}, ${a.args.targetYaw}, ${a.args.epsilonYaw}).withTimeout(${a.args.timeout})`;
            break;
          case 'arm':
            c = `new Arm(${a.args.pos}, ${a.args.angle}).withTimeout(${a.args.timeout})`;
            break;
          case 'claw':
            c = `new Claw(${a.args.power}).withTimeout(${a.args.timeout})`;
            break;
          case 'elevatorMax':
            c = `new Elevator_max()`;
            break;
          case 'elevatorMin':
            c = `new Elevator_min()`;
            break;
          case 'elevator':
            c = `new Elevator(${a.args.height}, ${a.args.eps}).withTimeout(${a.args.timeout})`;
            break;
          case 'turnToAngle':
            c = `new TurnToAngle(${a.args.angle}, ${a.args.eps})`;
            break;
          case 'cobra':
            c = `new Cobra(${a.args.a}, ${a.args.b}, ${a.args.c})`;
            break;
          case 'mazeNavigator':
            c = `new MazeNavigator(${a.args.target}, ${a.args.eps})`;
            break;
          case 'simpleDrive':
            c = `new SimpleDrive(${a.args.vx}, ${a.args.vy}, ${a.args.w})`;
            break;
          default:
            c = `// неизвестное действие: ${a.name}`;
        }
        cmds.push(`            ${c},`);
      });
    }

    cmds.push('            new StopMotors()');
    cmds.push('        );');
    cmds.push('    }');
    cmds.push('}');

    const blob = new Blob([cmds.join('\n')], { type: 'text/plain' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'DriveMotor.java';
    a.click();
    App.log && App.log('DriveMotor.java экспортирован (heading=yaw 1:1)');
  };

})();
