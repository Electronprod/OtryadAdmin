const calendar = document.querySelector('.calendar-line');
let calendar_data = null;
async function onResize() {
	try {
		if (calendar_data === null) {
			calendar_data = await fetchData("/api/line_calendar?login=" + document.getElementById("squadcommander_login").value);
			calendar_data.sort((a, b) => a.eventid - b.eventid);
			document.getElementById('events_number').innerText = calendar_data.length;
			document.getElementById('people_missed').innerText = await simValuesFromJSON(calendar_data, "absent");
			document.getElementById('people_presented').innerText = await simValuesFromJSON(calendar_data, "present");
			fillWeekData(calendar_data);
		}
		updateCalendar(calendar, calendar_data);
	} catch {
		alert("Произошла неизвестная ошибка");
	}
}
function calendar_button(date, event) {
	const path = window.location.pathname.replace(/\/+$/, '');
	const id = path.split('/').pop();
	window.location.href = `/observer/stats/squad/${id}/date?date=${date}&eventid=${event}`;
}
function showAllEvents() {
	insertTable("showAllEvents", calendar_data.slice().reverse());
}
function fillWeekData(calendar_data) {
	const lastDate = new Date();
	const startOfLastWeek = new Date(lastDate);
	startOfLastWeek.setDate(lastDate.getDate() - 6);
	const lastWeekEvents = calendar_data.filter(event => {
		const eventDate = new Date(event.date);
		return eventDate >= startOfLastWeek && eventDate <= lastDate;
	});
	const totalAbsent = lastWeekEvents.reduce((sum, event) => sum + event.absent, 0);
	const totalPresent = lastWeekEvents.reduce((sum, event) => sum + event.present, 0);
	const totalEvents = lastWeekEvents.length;
	document.getElementById('last_missed').innerText = totalAbsent;
	document.getElementById('last_presented').innerText = totalPresent;
	document.getElementById('last_events_number').innerText = totalEvents;
}
document.getElementById("date").valueAsDate = new Date();
function submitForm() {
	const form = document.getElementById('date-form');
	const dateInput = document.getElementById('date').value;
	const currentUrl = window.location.href;
	const newUrl = `${currentUrl}/date?${encodeURIComponent(dateInput)}`;
	form.action = newUrl;
	return true;
}
document.addEventListener("DOMContentLoaded", function() {
	var submitButton = document.getElementById("reportbtn");
	submitButton.setAttribute("formaction", window.location + "/report");
});
document.addEventListener("DOMContentLoaded", function() {
	var submitButton = document.getElementById("eventbtn");
	submitButton.setAttribute("formaction", window.location + "/event_table");
});