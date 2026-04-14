// Loading dependencies
loadScript("/assets/loader_modal.js", null);
if ((typeof Swal === "function") == false) {
	loadScript("/public_resources/sweetalert2.js", null);
}
async function sendData(address, bodyData) {
	const checkServerResult = await checkIfMarkedAlready(bodyData.event, bodyData.date);
	if (!checkServerResult.proceed) {
		if (checkServerResult.error) {
			showError("Отметки не выставлены", "Возникла ошибка при проверке данных на сервере");
		}
		return;
	}
	try {
		showLoader();
		const csrfToken = document.querySelector('input[name="_csrf"]').value;
		if (!csrfToken) {
			showError('Не найден токен CSRF!', "Перезагрузите страницу. Если не поможет, свяжитесь с разработчиком.");
			return false;
		}
		const response = await fetch(address, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'X-CSRF-TOKEN': csrfToken,
			},
			body: JSON.stringify(bodyData),
		});
		if (!response.ok) {
			let a = await response.json();
			console.log("Error message: " + JSON.stringify(a));
			if (JSON.stringify(a).includes("message") && JSON.stringify(a).includes("result") && JSON.stringify(a).includes("fail")) {
				showError("Ошибка HTTP " + response.status, "Некорректный ответ от сервера: " + a.message, "error");
			} else {
				showError("Ошибка HTTP " + response.status, "Некорректный ответ от сервера.", "error");
			}
			return false;
		}
		const data = await response.json();
		showSuccessMark();
		console.log('mark success:', data);
		const eventId = data.event_id ?? data.eventId ?? null;
		return { success: true, eventId }
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
		timerProgressBar: true,
		icon: "success",
		timer: 5000
	});
}
function showSuccessMark() {
	Swal.fire({
		title: "Отметки отправлены",
		text: "Дождитесь подтверждения от сервера, прежде чем покинуть эту страницу.",
		timerProgressBar: false,
		showConfirmButton: false,
		icon: "info",
		closeOnClickOutside: false
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
	showError(`Ошибка при выставлении отметок:\n${error}`);
}
async function getData(url) {
	try {
		const response = await fetch(url);
		if (!response.ok) {
			Swal.fire({
				title: "Ошибка!",
				text: "Ошибка при получении информации. Пожалуйста, перезагрузите страницу. \nОшибка: " + error,
				icon: "error"
			});
			throw new Error(`Ошибка HTTP: ${response.status}`);
		}
		return await response.json();
	} catch (error) {
		console.error('Ошибка при получении данных:', error);
		Swal.fire({
			title: "Ошибка!",
			text: "Ошибка при получении информации. Пожалуйста, перезагрузите страницу. \nОшибка: " + error,
			icon: "error"
		});
		return "";
	}
}
async function submitData(callback) {
	Swal.fire({
		title: 'Вы уверены?',
		text: "Пожалуйста, подтвердите правильность введенных данных.",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		confirmButtonText: 'Да, отправить',
		cancelButtonText: 'Отмена'
	}).then((result) => {
		if (result.isConfirmed && typeof callback === 'function') {
			callback();
		} else if (result.isConfirmed) {
			showError("Не удалось вызвать фунцию отправки данных!?");
		}
	});
}
var isAnyColumnHidden = false;
function hideColumn(elementID, columnIndex) {
	console.log('Hiding column ' + columnIndex + " in " + elementID);
	const table = document.getElementById(elementID);
	const rows = table.rows;
	for (let i = 0; i < rows.length; i++) {
		const cells = rows[i].cells;
		if (cells.length > columnIndex) {
			cells[columnIndex].style.display = 'none';
		}
	}
	isAnyColumnHidden = true;
}
function showColumn(elementID, columnIndex) {
	console.log('Showing column ' + columnIndex + " in " + elementID);
	const table = document.getElementById(elementID);
	const rows = table.rows;
	for (let i = 0; i < rows.length; i++) {
		const cells = rows[i].cells;
		if (cells.length > columnIndex) {
			cells[columnIndex].style.display = '';
		}
	}
	isAnyColumnHidden = false;
}
async function checkIfMarkedAlready(event, date) {
	try {
		const url = `/api/check_already_marked?event=${encodeURIComponent(event)}&date=${encodeURIComponent(date)}`;
		let response = await fetch(url);
		let code = response.status;
		if (code == 200) {
			return { proceed: true, error: false };
		} else if (code == 409) {
			let result = await Swal.fire({
				title: "Обнаружены похожие отметки!",
				html: "<p>Отметки с таким же событием в эту дату были найдены на сервере.</p> <br><b>Продолжить выставление?</b> ",
				icon: "warning",
				showCancelButton: true,
				confirmButtonColor: '#3085d6',
				cancelButtonColor: '#d33',
				confirmButtonText: 'Да, продолжить',
				cancelButtonText: 'Отмена'
			});
			console.log("[checkIfMarkedAlready]: User decision is", result.isConfirmed);
			return { proceed: result.isConfirmed, error: false };
		}
		console.warn("Error checking marks existence because the answer code was unrecognizable. Code: ", code);
		return { proceed: false, error: true };
	} catch (err) {
		console.error("Network error checking marks existence:", err);
		return { proceed: false, error: true };
	}
}