"use strict";

document.addEventListener("submit", (event) => {
  const message = event.target.dataset.confirm;
  if (message && !window.confirm(message)) {
    event.preventDefault();
  }
});

document.addEventListener("click", (event) => {
  const open = event.target.closest("[data-sidebar-open]");
  const close = event.target.closest("[data-sidebar-close]");
  const sidebar = document.getElementById("library-sidebar");
  const scrim = document.querySelector(".scrim");
  if (!sidebar || !scrim) return;
  if (open) {
    sidebar.classList.add("sidebar-open");
    scrim.hidden = false;
  } else if (close) {
    sidebar.classList.remove("sidebar-open");
    scrim.hidden = true;
  }
});
