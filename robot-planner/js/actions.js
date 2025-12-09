/* actions.js
   Расширенный реестр действий под WPILib auto-команды.
*/
window.App = window.App || {};

const actionRegistry = {
  // === Базовые ===
  pause: {
    label: 'Пауза',
    args: [{ name: 'duration', type: 'number', label: 'Длительность (мс)', default: 1000 }],
  },
  resetYaw: {
    label: 'resetYaw',
    args: [{ name: 'timeout', type: 'number', label: 'Timeout (с)', default: 0.1 }],
  },
  stopMotors: {
    label: 'StopMotors',
    args: [],
  },
  simpleDrive: {
    label: 'SimpleDrive',
    args: [
      { name: 'vx', type: 'number', label: 'X скорость', default: 0.0 },
      { name: 'vy', type: 'number', label: 'Y скорость', default: 0.0 },
      { name: 'w',  type: 'number', label: 'Поворот', default: 0.0 }
    ],
  },

  // === Движение ===
  driveEnc: {
    label: 'DriveEnc',
    args: [
      { name: 'left', type: 'number', label: 'Левое (мм)', default: 0 },
      { name: 'right', type: 'number', label: 'Правое (мм)', default: 0 },
      { name: 'epsL', type: 'number', label: 'ε лев.', default: 0.1 },
      { name: 'epsR', type: 'number', label: 'ε прав.', default: 0.1 },
      { name: 'yaw', type: 'number', label: 'Yaw', default: 0.0 },
      { name: 'epsYaw', type: 'number', label: 'ε Yaw', default: 0.1 },
    ],
  },
  strafe: {
    label: 'strafe',
    args: [
      { name: 'dist', type: 'number', label: 'Расстояние (мм)', default: 0 },
      { name: 'eps', type: 'number', label: 'ε', default: 0.1 },
      { name: 'yaw', type: 'number', label: 'Yaw', default: 0.0 },
      { name: 'epsYaw', type: 'number', label: 'ε Yaw', default: 0.1 },
    ],
  },
  turnToAngle: {
    label: 'TurnToAngle',
    args: [
      { name: 'angle', type: 'number', label: 'Угол (°)', default: 0 },
      { name: 'eps', type: 'number', label: 'ε', default: 0.1 },
    ],
  },
  sensor: {
    label: 'Sensor',
    args: [
      { name: 'targetRightX', type: 'number', label: 'Right X', default: 0 },
      { name: 'targetLeftX', type: 'number', label: 'Left X', default: 0 },
      { name: 'epsilonX', type: 'number', label: 'ε X', default: 0.1 },
      { name: 'targetLeftY', type: 'number', label: 'Left Y', default: 0 },
      { name: 'targetRightY', type: 'number', label: 'Right Y', default: 0 },
      { name: 'epsilonY', type: 'number', label: 'ε Y', default: 0.1 },
      { name: 'targetYaw', type: 'number', label: 'Yaw', default: 0.0 },
      { name: 'epsilonYaw', type: 'number', label: 'ε Yaw', default: 0.1 },
      { name: 'timeout', type: 'number', label: 'Timeout (с)', default: 5 },
    ],
  },
  sensorLidar: {
    label: 'Sensor_Lidar',
    args: [
      { name: 'targetRightX', type: 'number', label: 'Right X', default: 0 },
      { name: 'targetLeftX', type: 'number', label: 'Left X', default: 0 },
      { name: 'epsilonX', type: 'number', label: 'ε X', default: 0.1 },
      { name: 'targetFrontY', type: 'number', label: 'Front Y', default: 0 },
      { name: 'targetBackY', type: 'number', label: 'Back Y', default: 0 },
      { name: 'epsilonY', type: 'number', label: 'ε Y', default: 0.1 },
      { name: 'targetYaw', type: 'number', label: 'Yaw', default: 0.0 },
      { name: 'epsilonYaw', type: 'number', label: 'ε Yaw', default: 0.1 },
      { name: 'timeout', type: 'number', label: 'Timeout (с)', default: 5 },
    ],
  },

  // === Механизмы ===
  arm: {
    label: 'Arm',
    args: [
      { name: 'pos', type: 'number', label: 'Позиция', default: 300 },
      { name: 'angle', type: 'number', label: 'Угол серво', default: 190 },
      { name: 'timeout', type: 'number', label: 'Timeout (с)', default: 3 },
    ],
  },
  claw: {
    label: 'Claw',
    args: [
      { name: 'power', type: 'number', label: 'Положение', default: 250 },
      { name: 'timeout', type: 'number', label: 'Timeout (с)', default: 2 },
    ],
  },
  elevatorMax: { label: 'Elevator_max', args: [] },
  elevatorMin: { label: 'Elevator_min', args: [] },
  elevator: {
    label: 'Elevator',
    args: [
      { name: 'height', type: 'number', label: 'Δ (мм)', default: -170 },
      { name: 'eps', type: 'number', label: 'ε', default: 0.1 },
      { name: 'timeout', type: 'number', label: 'Timeout (с)', default: 5 },
    ],
  },
  cobra: {
    label: 'Cobra',
    args: [
      { name: 'a', type: 'number', label: 'A', default: 0.0 },
      { name: 'b', type: 'number', label: 'B', default: 0.3 },
      { name: 'c', type: 'number', label: 'C', default: 0.0 },
    ],
  },
  mazeNavigator: {
    label: 'MazeNavigator',
    args: [
      { name: 'target', type: 'number', label: 'Target', default: 0.0 },
      { name: 'eps', type: 'number', label: 'ε', default: 0.1 },
    ],
  },
};

App.actionRegistry = actionRegistry;
App.getAction = (n) => actionRegistry[n] || null;
