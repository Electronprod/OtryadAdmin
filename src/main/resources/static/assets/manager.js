const sendPostData = async (url) => {
	var csrfToken = document.querySelector('input[name="_csrf"]').value;

	try {
		const urlEncodedData = new URLSearchParams("").toString();
		const response = await fetch(url, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
				'X-CSRF-Token': csrfToken // Передаем CSRF токен
			},
			body: urlEncodedData
		});

		if (!response.ok) {
			console.log("HTTP error: " + response);
			showNotification("Ошибка HTTP " + response.status, "Некорректный ответ от сервера.", "error");
			return false;
		}

		const responseData = await response.json();
		console.log('Response:', responseData);
		return true;
	} catch (error) {
		console.error('Error:', error);
		showNotification("Ошибка!", error.toString(), "error");
		return false;
	}
};

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
let currentSortColumn = -1;
let currentSortDir = 'asc';

function sortTable(columnIndex) {
	const table = document.getElementById("userTable");
	const tbody = table.querySelector('tbody');
	const rows = Array.from(tbody.rows);
	let dir = 'asc';

	if (currentSortColumn === columnIndex) {
		dir = currentSortDir === 'asc' ? 'desc' : 'asc';
	}

	currentSortColumn = columnIndex;
	currentSortDir = dir;

	rows.sort((a, b) => {
		const cellA = a.cells[columnIndex].innerText;
		const cellB = b.cells[columnIndex].innerText;

		if (dir === 'asc') {
			return cellA.localeCompare(cellB, 'ru', { numeric: true });
		} else {
			return cellB.localeCompare(cellA, 'ru', { numeric: true });
		}
	});

	rows.forEach(row => tbody.appendChild(row));
}
function removeLine(id) {
	var divsToDelete = document.querySelectorAll('tr[name="' + id + '"]');
	divsToDelete.forEach(function(div) {
		div.remove();
	});
}
function showModal() {
	document.getElementById('overlay').style.display = 'flex';
}
function closeModal() {
	document.getElementById('overlay').style.display = 'none';
}
async function fetchData(url) {
	try {
		const response = await fetch(url);
		if (!response.ok) {
			showNotification("Ошибка HTTP " + response.status, "Получить данные не удалось.", "error");
			return null;
		}
		return await response.json();
	} catch (error) {
		showNotification("Неизвестная ошибка", error, "error");
		return null;
	}
}