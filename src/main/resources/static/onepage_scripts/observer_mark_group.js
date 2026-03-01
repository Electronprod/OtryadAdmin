// Loading dependencies
loadScript("/assets/mark_engine.js", null);
// Setting current date
document.getElementById("dateField").valueAsDate = new Date()
// Check all checkboxes
document.getElementById('select_all_humans_checkbox').addEventListener('change', function() {
	const checkboxes = document.querySelectorAll('input[name="select"]');
	checkboxes.forEach((checkbox) => {
		checkbox.checked = this.checked;
	});
});
// -------------Send data-------------
async function send() {
	try {
		const presentPeople = [];
		// For every checkbox...
		const checkboxes = document.querySelectorAll('.custom-checkbox');
		checkboxes.forEach(checkbox => {
			if (checkbox.checked) {
				presentPeople.push(checkbox.value);
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
			unpresentPeople: [],
			event: event_type.value,
			date: document.getElementById("dateField").value,
			groupID: null
		};
		const result = await sendData("/admin/mark", data_to_send);
		if (result && result.eventId) {
			const expectedTotal =
				data_to_send.presentPeople.length +
				data_to_send.unpresentPeople.length;
			verifyMarks(result.eventId, data_to_send, expectedTotal);
		}
	} catch (error) {
		console.error("Error in send(): ", error);
		showError("Произошла неизвестная ошибка!");
	}
}