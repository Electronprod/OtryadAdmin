// Loading dependencies
loadScript("/assets/search_table.js", null);
// Setting current date
document.getElementById("dateField").valueAsDate = new Date();
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
document.addEventListener('DOMContentLoaded', function() {
	hideColumn("markTable", 3);
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
// -------------Send data-------------
async function send() {
	if (isAnyColumnHidden) {
		positive_send();
	} else {
		normal_send();
	}
}
async function positive_send() {
	try {
		const presentPeople = [];
		// For every checkbox...
		const checkboxes = document.querySelectorAll('.custom-checkbox');
		checkboxes.forEach(checkbox => {
			if (checkbox.checked) {
				presentPeople.push(
					checkbox.value
				);
			}
		});
		// Finding event type
		var event_type = document.getElementById('event_type');
		if (!event_type || event_type.value === "") {
			showError("Не найдено название события!");
			return;
		}
		const data_to_send = {
			presentPeople: presentPeople,
			event: event_type.value,
			date: document.getElementById("dateField").value
		};
		sendData("/commander/mark", data_to_send);
	} catch (error) {
		console.error("Error in send(): ", error);
		showError("Произошла неизвестная ошибка!");
	}
}
async function normal_send() {
	try {
		const unpresentPeople = [];
		const presentPeople = [];
		const checkboxes = document.querySelectorAll('.custom-checkbox');
		checkboxes.forEach(checkbox => {
			if (!checkbox.checked) {
				const row = checkbox.closest('tr');
				const reasonSelect = row ? row.querySelector('.details-input') : null;
				if (reasonSelect) {
					if (reasonSelect.value != "do_not_mark_user") {
						unpresentPeople.push(JSON.stringify(
							{
								id: checkbox.value,
								reason: reasonSelect.value
							}
						));
					}
				}
			} else {
				presentPeople.push(checkbox.value);
			}
		});
		var event_type = document.getElementById('event_type');
		if (!event_type || event_type.value === "") {
			showError("Не найден тип события!");
			return;
		}
		const data_to_send = {
			presentPeople: presentPeople,
			unpresentPeople: unpresentPeople,
			event: event_type.value,
			date: document.getElementById("dateField").value
		};
		sendData("/commander/mark_with_reasons", data_to_send);
	} catch (error) {
		showError("Произошла неизвестная ошибка!", String(error));
	}
}
function toggleColumn(selectElement) {
	if (selectElement.value == "true") {
		showColumn("markTable", 3);
	} else {
		hideColumn("markTable", 3);
	}
}