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
// -------------Groups-------------
let people = [];
let groupid = -1;
async function groupSelect(group = 'null') {
	if (group === "null") {
		await showSearchInput(true);
	} else {
		await showSearchInput(false);
	}
	answer = await getData("/api/get_group_members_squadcommander?groupname=" + group);
	people = answer.people;
	groupid = answer.groupid;
	console.log("Members of the group:", people.length, "Group:", group, "GroupID: ", answer.groupid);
	// Applying
	let table = document.getElementById("markTable");
	let rows = table.querySelectorAll("tbody tr");
	Array.from(rows).forEach(row => {
		if (!people.includes(row.getAttribute('name'))) {
			row.style.display = "none";
		} else {
			row.style.display = "table-row";
		}
	});
}
// -------------Page logic-------------
// Checks whether to show the reason column. Allows you to create your own event.
let eventTypesWithReasons = "";
let showEvents = [];
async function handleEventChange(selectElement) {
	// Get the events with reason selector
	if (eventTypesWithReasons === "") {
		eventTypesWithReasons = await getData("/api/get_event_types_with_reasons");
	}
	// Current value selected
	const selectedValue = selectElement.value;
	// Create a custom event logic branch
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
				groupid = -1;
				Swal.fire({
					title: "Ваше событие добавлено!",
					text: "Обратите внимание, что событие пропадет после перезагрузки страницы",
					icon: "success"
				});
			} else {
				selectElement.value = selectElement.options[0].value;
				console.log("Rejected adding custom event");
				handleEventChange(selectElement);
			}
		});
		return;
	}
	// Group determition
	let selectedOption = selectElement.options[selectElement.selectedIndex];
	let groupname = selectedOption.getAttribute('group');
	await groupSelect(groupname);
	console.log("Group for selected option is", groupname, "ID:", groupid);
	let indicator_div = document.getElementById("indicator_div");
	let indicator = document.getElementById("indicator");
	if (groupname == "null") {
		indicator_div.style.display = "none";
	} else {
		indicator_div.style.display = "";
		indicator.textContent = groupname;
	}
	// Show the "reason select" column or not?
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
		const presentPeople = [];
		const checkboxes = document.querySelectorAll('.custom-checkbox');
		checkboxes.forEach(checkbox => {
			const row = checkbox.closest('tr');
			if (row.style.display != "none") {
				if (!checkbox.checked) {
					const reasonSelect = row ? row.querySelector('.details-input') : null;
					if (reasonSelect) {
						if (!isAnyColumnHidden) {
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
				} else {
					presentPeople.push(checkbox.value);
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
			presentPeople: presentPeople,
			event: event_type.value,
			groupID: groupid,
			date: document.getElementById("dateField").value
		};
		sendData("/squadcommander/mark", data_to_send);
	} catch (error) {
		showError("Произошла неизвестная ошибка!", String(error));
	}
}