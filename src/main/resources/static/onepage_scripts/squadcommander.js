// Loading dependencies
loadScript("/assets/mark_engine.js", null);
loadScript("/assets/search_table.js", null);
// Setting current date
document.getElementById("dateField").valueAsDate = new Date()
// -------------Event listeners-------------
// Set each checkbox logic
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
// Logic of the checkbox, which checks all other checkboxes
document.getElementById('select_all_humans_checkbox').addEventListener('change', function() {
	const checkboxes = document.querySelectorAll('input[name="select"]');
	checkboxes.forEach((checkbox) => {
		checkbox.checked = this.checked;
		const index = checkbox.id.split('-')[1];
		const selectInput = document.getElementById('details-' + index);
		if (selectInput) {
			selectInput.disabled = checkbox.checked;
		}
	});
});
// Checks whether to show the reason column. Allows you to create your own event.
let eventTypesWithReasons = "";
let showEvents = [];
async function handleEventChange(selectElement) {
	if (eventTypesWithReasons === "") {
		eventTypesWithReasons = await getData("/api/get_event_types_with_reasons");
	}
	const selectedValue = selectElement.value;
	if (selectElement.value == "unknown-event") {
		console.log("Creating custom event...");
		Swal.fire({
			title: 'Введите название события',
			input: 'text',
			html: `
		    <label for="checkbox">
		      <input type="checkbox" id="checkbox"/>
		      Указывать причины отсутствия
		    </label>
		  `,
			showCancelButton: true,
			confirmButtonText: 'Сохранить',
		}).then((result) => {
			if (result.value) {
				const checkbox = Swal.getPopup().querySelector('#checkbox');
				console.log('Event: ', result.value, 'Checkbox:', checkbox.checked);
				if (eventTypesWithReasons.some(event => event.event === result.value)) {
					showError("Событие уже существует!", "Данное событие уже есть в селекторе. Посмотрите внимательнее.");
					return;
				}
				if (checkbox.checked) {
					showColumn("markTable", 3);
					showEvents.push(result.value);
				} else {
					hideColumn("markTable", 3);
				}
				const option = document.createElement('option');
				option.value = result.value;
				option.textContent = result.value;
				selectElement.add(option);
				selectElement.value = result.value;
				Swal.fire({
					title: "Ваше событие добавлено!",
					text: "Обратите внимание, что событие пропадет после перезагрузки страницы",
					icon: "success"
				});
			}
		});
		return;
	}
	if (!eventTypesWithReasons.some(event => event.event === selectedValue)) {
		if (!showEvents.some(event => event === String(selectedValue))) {
			hideColumn("markTable", 3);
			return;
		}
	}
	showColumn("markTable", 3);
}
// -------------Send data-------------
async function send() {
	try {
		const unpresentPeople = [];
		const checkboxes = document.querySelectorAll('.custom-checkbox');
		checkboxes.forEach(checkbox => {
			if (!checkbox.checked) {
				const row = checkbox.closest('tr');
				const reasonSelect = row ? row.querySelector('.details-input') : null;
				if (reasonSelect) {
					if (isAnyColumnHidden) {
						unpresentPeople.push(JSON.stringify(
							{
								id: checkbox.value,
								reason: "error:unsupported_event"
							}
						));
					} else {
						unpresentPeople.push(JSON.stringify(
							{
								id: checkbox.value,
								reason: reasonSelect.value
							}
						));
					}
				} else {
					console.warn(`Error finding 'details-input' for id: ${checkbox.value}`);
				}
			}
		});
		var event_type = document.getElementById('event_type');
		if (!event_type) {
			showError("Не найден тип события!");
			return;
		}
		const data_to_send = {
			unpresentPeople: unpresentPeople,
			event: event_type.value,
			date: document.getElementById("dateField").value
		};
		sendData("/squadcommander/mark", data_to_send);
	} catch (error) {
		showError("Произошла неизвестная ошибка!", String(error));
	}
}