loadScript("/assets/loader_modal.js", null);
if ((typeof Swal === "function") == false) {
	loadScript("/public_resources/sweetalert2.js", null);
}
let small = false;
function setSmall(val) {
	small = val;
}
async function sendData(formElement) {
	try {
		showLoader();
		const formData = new FormData(formElement);
		const csrfToken = formData.get('_csrf');
		if (!csrfToken) {
			showError('Не найден токен CSRF!');
			return false;
		}
		const response = await fetch(formElement.action, {
			method: 'POST',
			headers: {
				'X-CSRF-TOKEN': csrfToken
			},
			body: formData
		});
		if (!response.ok) {
			const errorResponse = await response.json();
			console.log("Error message: " + JSON.stringify(errorResponse));
			if (errorResponse.message && errorResponse.result === 'fail') {
				showError("Ошибка HTTP " + response.status, "Некорректный ответ от сервера: " + errorResponse.message, "error");
			} else {
				showError("Ошибка HTTP " + response.status, "Некорректный ответ от сервера.", "error");
			}
			return false;
		}
		const data = await response.json();
		showSuccess("Действие выполнено успешно");
		console.log('result:', data);
		return true;
	} catch (error) {
		handleError(error);
		return false;
	} finally {
		hideLoader();
	}
}
function showSuccess(message) {
	if (small) {
		showNotification("Успех!", message, "success");
		return;
	}
	Swal.fire({
		title: "Успех!",
		text: message,
		icon: "success",
	});
}
function showError(message) {
	Swal.fire({
		title: "Провал!",
		text: message,
		icon: "error",
	});
}
function showError(title, message) {
	if (small) {
		showNotification(title, message, "error");
		return;
	}
	Swal.fire({
		title: title,
		text: message,
		icon: "error",
	});
}
function handleError(error) {
	console.error('mark error:', error);
	showError(`Ошибка при обработке:\n${error}`);
}
function showNotification(title, text, icon) {
	Swal.fire({
		title: title,
		text: text,
		icon: icon,
		toast: true,
		position: 'top-end',
		showConfirmButton: false,
		timer: 3000,
		timerProgressBar: true,
		showClass: {
			popup: 'swal2-show'
		},
		hideClass: {
			popup: 'swal2-hide'
		}
	});
}
