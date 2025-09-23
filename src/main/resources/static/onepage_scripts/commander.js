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
let people = [];
async function groupSelect() {
	// Retrieving data
	let group = document.getElementById('group').value;
	if (group === "all_people")
		group = null;
	answer = await getData("/api/get_group_members?group=" + group);
	people = answer.people;
	console.log("Members of the group:", people.length, "Group:", group, "Allow 'unmarked': ", !answer.mark);
	// Applying
	let table = document.getElementById("markTable");
	let rows = table.querySelectorAll("tbody tr");
	Array.from(rows).forEach(row => {
		if (!people.includes(row.getAttribute('name'))) {
			row.style.display = "none";
			if (answer.mark)
				setNonMarkingOptions("none");
		} else {
			row.style.display = "table-row";
			setNonMarkingOptions("flex");
		}
	});
}
function setNonMarkingOptions(value) {
	let options = document.querySelectorAll("option.non_marking");
	let reasons = document.querySelectorAll("select.details-input");
	options.forEach(option => {
		option.style.display = value;
	});
	if (value === "none") {
		reasons.forEach(reason => {
			reason.selectedIndex = 1;
		});
	} else {
		reasons.forEach(reason => {
			reason.selectedIndex = 0;
		});
	}
}
// -------------Send data-------------
async function send() {
	try {
		let unpresentPeople = [];
		let presentPeople = [];
		let checkboxes = document.querySelectorAll('.custom-checkbox');
		let group = document.getElementById('group').value;
		if (group === "all_people")
			group = null;
		checkboxes.forEach(checkbox => {
			const row = checkbox.closest('tr');
			if (row.style.display != "none") {
				if (!checkbox.checked) {
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
			}
		});
		let event_type = document.getElementById('event_type');
		if (!event_type || event_type.value === "") {
			showError("Не найден тип события!");
			return;
		}
		let data_to_send = {
			presentPeople: presentPeople,
			unpresentPeople: unpresentPeople,
			event: event_type.value,
			date: document.getElementById("dateField").value,
			groupID: group
		};
		sendData("/commander/mark", data_to_send);
	} catch (error) {
		showError("Произошла неизвестная ошибка!", String(error));
	}
}