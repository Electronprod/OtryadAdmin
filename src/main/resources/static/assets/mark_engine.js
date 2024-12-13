// Loading dependencies
loadScript("/assets/loader_modal.js", null);
if ((typeof Swal === "function") == false) {
	loadScript("/public_resources/sweetalert2.js", null);
}
async function sendData(address, bodyData) {
	try {
		showLoader();
		const csrfToken = document.querySelector('input[name="_csrf"]').value;
		if (!csrfToken) {
			showError('Не найден токен CSRF!');
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
			showError(`Ошибка HTTP: ${response.status}`);
			return false;
		}
		const data = await response.json();
		showSuccess("Ребята отмечены!");
		console.log('mark success:', data);
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
