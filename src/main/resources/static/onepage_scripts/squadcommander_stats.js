const calendar = document.querySelector('.calendar-line');
let calendar_data = null;
async function onResize() {
	try {
		if (calendar_data === null) {
			calendar_data = await fetchData("/api/line_calendar");
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
	window.location.href = `/squadcommander/stats/date?date=${date}&eventid=${event}`;
}
function showAllEvents() {
	insertTable("showAllEvents", calendar_data);
}
function fillWeekData(calendar_data) {
	const lastDate = new Date(calendar_data[calendar_data.length - 1].date);
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
document.getElementById("dateField").valueAsDate = new Date();