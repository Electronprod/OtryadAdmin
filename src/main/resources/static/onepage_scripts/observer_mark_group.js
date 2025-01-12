// Loading dependencies
loadScript("/assets/mark_engine.js", null);
// Setting current date
document.getElementById("dateField").valueAsDate = new Date()
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
// -------------Send data-------------
const groupID = document.getElementById('groupid').value;
const groupName = document.getElementById('groupname').value;
async function send() {
	try {
		const presentPeople = [];
		const unpresentPeople = [];
		// For every checkbox...
		const checkboxes = document.querySelectorAll('.custom-checkbox');
		checkboxes.forEach(checkbox => {
			if (!checkbox.checked) {
				const row = checkbox.closest('tr');
				const reasonSelect = row ? row.querySelector('.details-input') : null;
				if (reasonSelect) {
					unpresentPeople.push(JSON.stringify(
						{
							id: checkbox.value,
							reason: reasonSelect.value
						}
					));
				} else {
					console.warn(`Не удалось найти 'details-input' для элемента с id: ${checkbox.value}`);
				}
			} else {
				presentPeople.push(checkbox.value);
			}
		});
		// Finding event type
		var event_type = document.getElementById('event_type');
		if (!event_type || event_type.value === "") {
			showError("Не найдено название события!");
			//event_type.scrollIntoView({ block: "center", behavior: "smooth" });
			return;
		}
		const data_to_send = {
			presentPeople: presentPeople,
			unpresentPeople: unpresentPeople,
			event: event_type.value,
			date: document.getElementById("dateField").value,
			groupID: groupID
		};
		sendData("/observer/mark_group", data_to_send);
	} catch (error) {
		console.error("Error in send(): ", error);
		showError("Произошла неизвестная ошибка!");
	}
}