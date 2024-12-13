// Loading dependencies
loadScript("/assets/mark_engine.js", null);
// Setting current date
document.getElementById("dateField").valueAsDate = new Date()
// -------------Event listeners-------------
// Checkbox worker
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
// Check all checkboxes
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
//Search
function filterTable() {
	const input = document.getElementById('searchInput');
	const filter = input.value.toLowerCase();
	const table = document.getElementById('markTable');
	const tbody = table.getElementsByTagName('tbody')[0];
	const rows = tbody.getElementsByTagName('tr');

	for (let i = 0; i < rows.length; i++) {
		const lastname = rows[i].getElementsByTagName('td')[1];
		const firstname = rows[i].getElementsByTagName('td')[2];

		if (lastname || firstname) {
			const lastnameText = lastname.textContent.toLowerCase();
			const firstnameText = firstname.textContent.toLowerCase();

			if (lastnameText.indexOf(filter) > -1 || firstnameText.indexOf(filter) > -1) {
				rows[i].style.display = '';
			} else {
				rows[i].style.display = 'none';
			}
		}
	}
}
//Search
document.getElementById("searchInput").addEventListener("keypress", function() {
	this.scrollIntoView(true);
});
// Checks whether to show the reason column. Allows you to create your own event.
let eventTypesWithReasons = "";
async function handleEventChange(selectElement) {
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
				console.log('Event: ', result.value);
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
		showColumn("markTable", 3);
		return;
	}
	if (eventTypesWithReasons === "") {
		eventTypesWithReasons = await getData("/api/get_event_types_with_reasons");
	}
	if (!eventTypesWithReasons.some(event => event.event === selectedValue)) {
		hideColumn("markTable", 3);
	} else {
		showColumn("markTable", 3);
	}
}
// -------------Send data-------------
async function send() {
	try {
		const unpresentPeople = [];
		// For every checkbox...
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
		// Finding event type
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
		showError("Произошла неизвестная ошибка!");
	}
}
