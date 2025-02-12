loadScript("/assets/loader_modal.js", null);
if ((typeof Swal === "function") == false) {
	loadScript("/public_resources/sweetalert2.js", null);
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
document.getElementById('sendremaind').addEventListener('submit', function(event) {
	event.preventDefault();
	sendData(this);
});
document.getElementById('sendmessage').addEventListener('submit', function(event) {
	event.preventDefault();
	sendData(this);
});
