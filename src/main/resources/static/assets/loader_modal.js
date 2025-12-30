const loaderstyle = document.createElement('style');
loaderstyle.textContent = `
#loader {
  position: fixed;
  top: 50%;
  left: 50%;
  width: 50px;
  height: 50px;
  margin: -25px 0 0 -25px;
  border: 6px solid #ccc;
  border-top: 6px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  z-index: 9999;
  display: none;
}
#loader-text {
  position: fixed;
  top: calc(50% + 35px);
  left: 50%;
  transform: translate(-50%, 0);
  font-size: 16px;
  font-family: Arial, sans-serif;
  color: #3498db;
  z-index: 9999;
  display: none;
}
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}`;
document.head.appendChild(loaderstyle);
let loader_modal = document.getElementById('loader');
if (!loader_modal) {
	loader_modal = document.createElement('div');
	loader_modal.id = 'loader';
	document.body.insertBefore(loader_modal, document.body.firstChild);
}
let loader_text = document.getElementById('loader-text');
if (!loader_text) {
	loader_text = document.createElement('div');
	loader_text.id = 'loader-text';
	loader_text.textContent = 'Выполняю';
	document.body.insertBefore(loader_text, document.body.firstChild);
}
console.debug("[LOADER_MODAL]: loaded. Object: " + loader_modal);
function showLoader() {
	loader_modal.style.display = 'block';
	loader_text.style.display = 'block';
}
function hideLoader() {
	loader_modal.style.display = 'none';
	loader_text.style.display = 'none';
}
function loader(new_loader) {
	loader_modal = document.getElementById(new_loader);
	console.debug("[LOADER_MODAL]: updated to " + loader_modal);
}
function loader() {
	return loader_modal;
}

