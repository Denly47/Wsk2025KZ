import './state.js';
import { setLogElement } from './logger.js';
import './actions.js';
import './drawing.js';
import './simulation.js';
import './ui-topbar.js';
import './ui-sidebar.js';
import './ui-canvas.js';

// привязываем лог-элемент и кнопки сворачивания
window.addEventListener('DOMContentLoaded', ()=>{
  const el = document.getElementById('logOutput');
  if (el) setLogElement(el);

  const topbar = document.getElementById('topbar');
  const sidebar = document.getElementById('sidebar');
  const workspace = document.getElementById('workspace');
  const tBtn = document.getElementById('toggleTopbar');
  const sBtn = document.getElementById('toggleSidebar');

  const syncTopBtn = ()=> tBtn.textContent = topbar.classList.contains('collapsed') ? '▼' : '▲';
  const syncSidBtn = ()=> sBtn.textContent = sidebar.classList.contains('collapsed') ? '☰' : '⟨';

  tBtn.addEventListener('click', ()=>{
    topbar.classList.toggle('collapsed');
    syncTopBtn();
  });
  sBtn.addEventListener('click', ()=>{
    sidebar.classList.toggle('collapsed');
    workspace.classList.toggle('sidebar-collapsed');
    syncSidBtn();
  });

  syncTopBtn(); syncSidBtn();
});
