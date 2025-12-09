/* logger.js
   Простой логгер в левую панель.
*/
let _logEl = null;

export function setLogElement(el){ _logEl = el; }

export function log(msg){
  if(!_logEl) return;
  const time = new Date().toLocaleTimeString();
  _logEl.textContent += `[${time}] ${msg}\n`;
  _logEl.scrollTop = _logEl.scrollHeight;
}

// Экспортируем в App для удобного вызова из других модулей
window.App = window.App || {};
App.setLogElement = setLogElement;
App.log = log;
