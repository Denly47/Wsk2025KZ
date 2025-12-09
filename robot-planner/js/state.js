/* state.js
   Глобальное состояние приложения: поле, точки, объекты, ассистенты, робот.
*/
window.App = window.App || {};

App.state = {
  // Поле/холст
  fieldWcm: 300,
  fieldHcm: 200,
  canvasWpx: 900,
  canvasHpx: 600,

  // Маршрут
  points: [],        // [{x,y,heading,actions:[{name,args,parallel}]}]

  // Статичные объекты карты (стены, полки, кубики, цилиндры, зоны)
  // shape: 'rect' | 'circle'
  // type: 'wall' | 'shelf' | 'box' | 'cylinder' | 'zone' | 'custom'
  objects: [],

  // Режим работы холста
  // add | move | rotate | actions | objects | measure
  mode: 'add',

  // Выделение точки маршрута
  selected: null,

  // Выделенный объект (по id)
  selectedObjectId: null,

  // Ассистенты рисования
  assistStraight: false, // фиксация X/Y к предыдущей точке (линии строго горизонт/вертикаль). Shift — временно.
  assistTankSnap: true,  // для Tank: прилипать к 0/90/180/270 при добавлении/перемещении
  angleSnap: true,       // квантизация угла
  angleSnapStep: 5,      // шаг кванта (1/5/10/15 градусов)

  // Инструмент «линейка»
  measure: {
    enabled: false,
    points: [] // [{x,y}] в см
  }
};

App.robot = {
  // Положение и ориентация в см/градусах (использует те же единицы, что и поле)
  x: 0,
  y: 0,
  heading: 0,

  // Габариты робота (визуализация прямоугольником)
  width: 30,
  height: 30,

  // Ориентация 0° относительно поля: down/up/right/left
  zero: 'down',

  // Параметры движения
  speed: 50,           // см/с
  drive: 'Omni',       // 'Omni' | 'Tank'

  // Для симуляции
  running: false,
  targetIndex: 0,
  targetAngle: null,
  actionDelayMs: 0
};
