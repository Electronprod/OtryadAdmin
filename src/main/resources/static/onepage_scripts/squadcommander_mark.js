document.getElementById('selectAll').addEventListener('change', function() {
	const checkboxes = document.querySelectorAll('input[name="selectedIds"]');
	checkboxes.forEach((checkbox) => {
		checkbox.checked = this.checked;
		const index = checkbox.id.split('-')[1];
		const selectInput = document.getElementById('details-' + index);
		if (selectInput) {
			selectInput.disabled = checkbox.checked;
		}
	});
});
async function fetchData(url) {
	try {
		const response = await fetch(url);
		if (!response.ok) {
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
function submitData() {
	Swal.fire({
		title: 'Вы уверены?',
		text: "Вы точно выбрали всех ребят? \n Вы выбрали тот тип статистики? Выбран тип статистики: " + getSelectedText(),
		icon: 'warning',
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		confirmButtonText: 'Да, отправить',
		cancelButtonText: 'Отмена'
	}).then((result) => {
		if (result.isConfirmed) {
			sendData();
		}
	});
}

async function sendData() {
	const uncheckedPeople = [];
	// Проходим по всем чекбоксам
	const checkboxes = document.querySelectorAll('.custom-checkbox');
	checkboxes.forEach(checkbox => {
		if (!checkbox.checked) {
			const row = checkbox.closest('tr');
			const reasonSelect = row ? row.querySelector('.details-input') : null;
			if (reasonSelect) {
				uncheckedPeople.push(JSON.stringify(
					{
						id: checkbox.value,
						reason: reasonSelect.value // Получаем значение причины
					}
				));
			} else {
				console.warn(`Не удалось найти 'details-input' для элемента с id: ${checkbox.value}`);
			}
		}
	});
	var selectElement = document.getElementById('statsType');
	const bodyData = {
		uncheckedPeople: uncheckedPeople,
		statsType: selectElement ? selectElement.value : null
	};
	if (!selectElement) {
		console.error('Element with id "statsType" not found.');
		Swal.fire({
			title: "Ошибка!",
			text: "Не удалось найти элемент для выбранного типа статистики. Пожалуйста, обновите страницу.",
			icon: "error"
		});
		return;
	}
	var csrfTokenInput = document.querySelector('input[name="_csrf"]');
	var csrfToken = csrfTokenInput ? csrfTokenInput.value : null;
	if (!csrfToken) {
		console.error('CSRF token input not found.');
		Swal.fire({
			title: "Ошибка!",
			text: "Не удалось найти CSRF токен. Пожалуйста, обновите страницу.",
			icon: "error"
		});
		return;
	}
	try {
		const response = await fetch('/squadcommander/mark', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'X-CSRF-TOKEN': csrfToken,
			},
			body: JSON.stringify(bodyData)
		});

		if (!response.ok) {
			console.log("HTTP error: " + response);
			Swal.fire({
				title: "Провал!",
				text: `Ошибка HTTP: ${response.status}\nНекорректный ответ от сервера.`,
				icon: "error"
			});
			return false;
		}

		const data = await response.json();
		console.log('mark success:', data);
		Swal.fire({
			title: "Успех!",
			text: "Ребята отмечены",
			icon: "success"
		});
		return true;
	} catch (error) {
		console.error('mark error:', error);
		Swal.fire({
			title: "Провал!",
			text: `Ошибка при выставлении отметок\n${error}`,
			icon: "error"
		});
		return false;
	}
}


function getSelectedText() {
	var selectElement = document.getElementById('statsType');
	var selectedIndex = selectElement.selectedIndex;
	var selectedText = selectElement.options[selectedIndex].text;
	return selectedText;
}
let eventTypesWithReasons = "";
async function handleSelectChange(selectElement) {
	const selectedValue = selectElement.value;
	if (selectElement.value == "unknown-event") {
		console.log("Creating custom event...");
		Swal.fire({
			title: 'Введите название события',
			text: "Так, чтобы можно было понять, что это.",
			input: 'text',
			showCancelButton: true,
			confirmButtonText: 'Сохранить',
		}).then((result) => {
			if (result.value) {
				console.log('User entered: ', result.value);
				var main = document.getElementById('statsType');
				const option = document.createElement('option');
				option.value = result.value;
				option.text = result.value;
				selectElement.add(option);
				selectElement.value = result.value;
				selectElement.disabled = true;
				Swal.fire({
					title: "Готово!",
					text: "Событие указано. Если вы хотите выбрать другое, перезагрузите страницу.",
					icon: "success"
				});
			}
		});
		showColumn(3);
		return;
	}
	if (eventTypesWithReasons === "") {
		eventTypesWithReasons = await fetchData("/api/get_event_types_with_reasons");
	}
	if (!eventTypesWithReasons.some(event => event.event === selectedValue)) {
		hideColumn(3);
	} else {
		showColumn(3);
	}
}
function hideColumn(columnIndex) {
	console.log('Hiding column');
	const table = document.getElementById('markTable');
	const rows = table.rows;
	for (let i = 0; i < rows.length; i++) {
		const cells = rows[i].cells;
		if (cells.length > columnIndex) {
			cells[columnIndex].style.display = 'none';
		}
	}
}
function showColumn(columnIndex) {
	console.log('Showing column');
	const table = document.getElementById('markTable');
	const rows = table.rows;
	for (let i = 0; i < rows.length; i++) {
		const cells = rows[i].cells;
		if (cells.length > columnIndex) {
			cells[columnIndex].style.display = '';
		}
	}
}
document.addEventListener('DOMContentLoaded', function() {
	const checkboxes = document.querySelectorAll('.custom-checkbox');

	checkboxes.forEach(checkbox => {
		checkbox.addEventListener('change', function() {
			const index = this.id.split('-')[1];
			const selectInput = document.getElementById('details-' + index);

			if (selectInput) {
				selectInput.disabled = this.checked;
			}
		});
	});
});