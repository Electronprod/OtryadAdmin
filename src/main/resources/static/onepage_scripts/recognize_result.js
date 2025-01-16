// Loading dependencies
loadScript("/assets/loader_modal.js", null);
if ((typeof Swal === "function") == false) {
	loadScript("/public_resources/sweetalert2.js", null);
}
async function add_group() {
	showLoader();
	hideLoader();
}
function showShortNotification(title, text, icon) {
	Swal.fire({
		title: title,
		text: text,
		icon: icon,
		toast: true,
		position: 'top-end',
		showConfirmButton: false,
		timer: 1000,
		timerProgressBar: true,
		showClass: {
			popup: 'swal2-show'
		},
		hideClass: {
			popup: 'swal2-hide'
		}
	});
}