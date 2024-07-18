if (window.location.href.includes("saved")) {
	Swal.fire({
		title: "УСПЕХ!",
		text: "Объект сохранен.",
		icon: "success"
	});
}
if (window.location.href.includes("edited")) {
	Swal.fire({
		title: "УПЕХ!",
		text: "Изменения сохранены в базу данных",
		icon: "success"
	});
}
if (window.location.href.includes("deleted")) {
	Swal.fire({
		title: "УПЕХ",
		text: "Объект удален.",
		icon: "info"
	});
}
if (window.location.href.includes("errorsquad")) {
	Swal.fire({
		title: "ОШИБКА: звено не найдено",
		text: "Звено не было найдено в базе данных.",
		icon: "error"
	});
}
if (window.location.href.includes("error_notfound")) {
	Swal.fire({
		title: "ОШИБКА: объект не найден",
		text: "Объект не был найден в базе данных.",
		icon: "error"
	});
}
if (window.location.href.includes("error")) {
	Swal.fire({
		title: "Произошла ошибка.",
		text: "Попробуйте еще раз.",
		icon: "error"
	});
}
if (window.location.href.includes("error_hasvalue")) {
	Swal.fire({
		title: "ОШИБКА: объект уже имеет значение",
		text: "Данный объект уже имеет установленное значение. Попробуйте его отредактировать",
		icon: "error"
	});
}
if (window.location.href.includes("error_protected")) {
	Swal.fire({
		title: "ОШИБКА: Действие запрещено",
		text: "Данный объект не может быть удален или изменен.",
		icon: "error"
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
function addF(url) {
	Swal.fire({
		title: 'Введите ID',
		input: 'number',
		inputAttributes: {
			step: '1',
			min: '1'
		},
		showCancelButton: true,
		confirmButtonText: 'Добавить',
		showLoaderOnConfirm: true,
		preConfirm: (number) => {
			if (!number || number % 1 !== 0) {
				Swal.showValidationMessage('ID - целое число!');
			}
		}
	}).then((result) => {
		if (result.value) {
			window.location.href = url + '/add?id=' + encodeURIComponent(result.value);
		}
	});
}
function addHuman(url) {
	Swal.fire({
		title: 'Введите ID звена',
		input: 'number',
		inputAttributes: {
			step: '1',
			min: '1'
		},
		showCancelButton: true,
		confirmButtonText: 'Добавить',
		showLoaderOnConfirm: true,
		preConfirm: (number) => {
			if (!number || number % 1 !== 0) {
				Swal.showValidationMessage('ID - целое число!');
			}
		}
	}).then((result) => {
		if (result.value) {
			window.location.href = url + '/add?id=' + encodeURIComponent(result.value);
		}
	});
}
function addSquad(url) {
	Swal.fire({
		title: 'Введите ID командира',
		input: 'number',
		inputAttributes: {
			step: '1',
			min: '1'
		},
		showCancelButton: true,
		confirmButtonText: 'Добавить',
		showLoaderOnConfirm: true,
		preConfirm: (number) => {
			if (!number || number % 1 !== 0) {
				Swal.showValidationMessage('ID - целое число!');
			}
		}
	}).then((result) => {
		if (result.value) {
			window.location.href = url + '/add?id=' + encodeURIComponent(result.value);
		}
	});
}
function editF(url) {
	Swal.fire({
		title: 'Введите ID',
		input: 'number',
		inputAttributes: {
			step: '1',
			min: '1'
		},
		showCancelButton: true,
		confirmButtonText: 'Редактировать',
		showLoaderOnConfirm: true,
		preConfirm: (number) => {
			if (!number || number % 1 !== 0) {
				Swal.showValidationMessage('ID - целое число!');
			}
		}
	}).then((result) => {
		if (result.value) {
			window.location.href = url + '/edit?id=' + encodeURIComponent(result.value);
		}
	});
}
function deleteF(url) {
	Swal.fire({
		title: 'Введите ID',
		input: 'number',
		inputAttributes: {
			step: '1',
			min: '1'
		},
		showCancelButton: true,
		confirmButtonText: 'Удалить',
		showLoaderOnConfirm: true,
		preConfirm: (number) => {
			if (!number || number % 1 !== 0) {
				Swal.showValidationMessage('ID - целое число!');
			}
		}
	}).then((result) => {
		if (result.value) {
			window.location.href = url + '/delete?id=' + encodeURIComponent(result.value);
		}
	});
}