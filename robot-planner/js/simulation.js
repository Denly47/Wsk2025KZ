/* simulation.js
   Простая симуляция движения:
   - Omni: прямая к цели; ориентация на отрезке не принудительна.
   - Tank: сначала доворачиваемся к направлению на цель, затем едем вперёд.
   В точках выполняются "actions" с задержками (если не parallel).
*/
import { log } from './logger.js';

window.App = window.App || {};
const state = App.state;
const robot = App.robot;

// Мягкие параметры симулятора
const CFG = {
  ARRIVE_EPS: 1.5,     // радиус достижения точки (см)
  ROT_EPS:    2.0,     // точность доворота (°)
  ROT_SPEED:  150,     // скорость поворота (°/с)
  MAX_DT:     0.05,    // кап dt
  STALL_EPS:  0.02,    // что считаем отсутствием прогресса по расстоянию (см)
  STALL_TIME: 1200,    // через сколько мс срабатывает защита
  SNAP_ON_STALL: false // если true — телепортируемся к точке при защите
};

let rafId=null, lastTs=0, lastDist=Infinity, stallTimerMs=0;

function normDeg(a){ a%=360; if(a<0)a+=360; return a; }
function angleDiffSigned(t,c){ return ((t - c + 540) % 360) - 180; }
function angleTo(p,q){ return normDeg(Math.atan2(q.y-p.y,q.x-p.x)*180/Math.PI); }
function segLen(p,q){ return Math.hypot(q.x-p.x, q.y-p.y); }

function validateTank(){
  for(let i=1;i<state.points.length;i++){
    if(segLen(state.points[i-1],state.points[i])<0.1)
      log(`ВНИМАНИЕ: отрезок ${i} слишком короткий`);
  }
}

function startSim(){
  if(state.points.length<1){ alert('Добавьте хотя бы одну точку.'); return; }
  if(robot.drive==='Tank') validateTank();

  // Старт: становимся в первую точку
  robot.running=true;
  robot.x=state.points[0].x; robot.y=state.points[0].y;
  robot.heading=state.points[0].heading||0;

  robot.targetIndex=1;
  robot.actionDelayMs=0;

  lastTs=performance.now(); lastDist=Infinity; stallTimerMs=0;
  log('Симуляция запущена');
  rafId=requestAnimationFrame(tick);
}

function stopSim(){
  if(rafId) cancelAnimationFrame(rafId);
  robot.running=false;
  robot.actionDelayMs=0;
  log('Симуляция остановлена');
  App.render();
}

function handleActions(pt){
  if(!pt.actions||pt.actions.length===0) return 0;
  log(`Точка достигнута. Действия: ${pt.actions.length}`);
  let delay=0;
  pt.actions.forEach(a=>{
    const def = App.getAction(a.name);
    log(`- ${a.name} ${JSON.stringify(a.args)} ${a.parallel?'(паралл.)':''}`);
    if(def&&def.run) def.run(a.args);
    if(!a.parallel){
      if(a.name==='pause' && a.args?.duration) delay += Number(a.args.duration)||0;
      else if(a.name==='servo1' && a.args?.time) delay += 1000*(Number(a.args.time)||1);
      else delay += 1000; // по умолчанию 1с
    }
  });
  if(delay>0) log(`Пауза ${ (delay/1000).toFixed(2) } с`);
  return delay;
}

function tick(ts){
  if(!robot.running) return;
  let dt=(ts-lastTs)/1000; lastTs=ts; if(dt>CFG.MAX_DT) dt=CFG.MAX_DT;

  // ожидание после действий
  if(robot.actionDelayMs>0){
    robot.actionDelayMs -= dt*1000;
    if(robot.actionDelayMs<0) robot.actionDelayMs=0;
    App.render(); rafId=requestAnimationFrame(tick); return;
  }

  // все точки пройдены
  if(robot.targetIndex>=state.points.length){
    log('Маршрут пройден.');
    stopSim(); return;
  }

  const target=state.points[robot.targetIndex];
  const dx=target.x-robot.x, dy=target.y-robot.y;
  const dist=Math.hypot(dx,dy);

  // защита от зависания: нет прогресса — засекаем время
  if(dist <= lastDist - CFG.STALL_EPS){
    lastDist=dist; stallTimerMs=0;
  } else {
    stallTimerMs += dt*1000;
    if(stallTimerMs>CFG.STALL_TIME){
      log('Защита: нет прогресса');
      if(CFG.SNAP_ON_STALL){ robot.x=target.x; robot.y=target.y; lastDist=0; }
      stallTimerMs=0;
    }
  }

  if(dist < CFG.ARRIVE_EPS){
    // дошли до координат — доворачиваемся до heading точки
    const need = (target.heading ?? robot.heading);
    const diffH = angleDiffSigned(need, robot.heading);
    const rotStep = CFG.ROT_SPEED*dt;
    if(Math.abs(diffH)>CFG.ROT_EPS){
      robot.heading = normDeg(robot.heading + Math.sign(diffH)*rotStep);
    }else{
      robot.heading = need;
      // действия в точке
      robot.actionDelayMs = handleActions(target);
      // следующая точка
      robot.targetIndex++; lastDist=Infinity; stallTimerMs=0;
    }
  } else {
    if(robot.drive==='Omni'){
      // едем по прямой к цели
      const step=Math.min(robot.speed*dt, dist);
      robot.x += (dx/dist)*step;
      robot.y += (dy/dist)*step;
    } else {
      // Tank: доворачиваемся к цели, потом едем вперёд
      const aim = angleTo({x:robot.x,y:robot.y}, target);
      const diff = angleDiffSigned(aim, robot.heading);
      const rotStep=CFG.ROT_SPEED*dt;
      if(Math.abs(diff)>CFG.ROT_EPS){
        robot.heading = normDeg(robot.heading + Math.sign(diff)*rotStep);
      }else{
        const step=Math.min(robot.speed*dt, dist);
        const rad=robot.heading*Math.PI/180;
        robot.x += Math.cos(rad)*step;
        robot.y += Math.sin(rad)*step;
      }
    }
  }

  App.render();
  rafId=requestAnimationFrame(tick);
}

App.startSim = startSim;
App.stopSim  = stopSim;
