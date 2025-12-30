document.getElementById("mark_table_datepickr").valueAsDate = new Date()
document.addEventListener("DOMContentLoaded", () => {
	loadMarkedUsersTable(formatDate(new Date()));
});
function loadMarkedUsersTable(date) {
	const url = `/api/observer/marks?date=${date}`;
	fetch(url)
		.then(response => response.json())
		.then(data => {
			const markedDiv = document.getElementById('marked');
			while (markedDiv.firstChild) {
				markedDiv.removeChild(markedDiv.firstChild);
			}
			const table = document.createElement('table');
			const thead = document.createElement('thead');
			const headerRow = document.createElement('tr');
			const headers = ['Роль', 'Имя', 'События', 'Кол-во событий'];
			headers.forEach(headerText => {
				const th = document.createElement('th');
				th.textContent = headerText;
				headerRow.appendChild(th);
			});
			thead.appendChild(headerRow);
			table.appendChild(thead);
			const tbody = document.createElement('tbody');
			if (data.length === 0) {
				const row = document.createElement('tr');
				const emptyCell = document.createElement('td');
				emptyCell.colSpan = headers.length;
				emptyCell.textContent = 'Нет выставленных отметок';
				row.appendChild(emptyCell);
				tbody.appendChild(row);
				document.getElementById('showStatsByDate').disabled = true;
			} else {
				document.getElementById('showStatsByDate').disabled = false;
				data.forEach(item => {
					const row = document.createElement('tr');
					const roleCell = document.createElement('td');
					roleCell.textContent = item.role;
					row.appendChild(roleCell);

					const nameCell = document.createElement('td');
					nameCell.textContent = item.name;
					row.appendChild(nameCell);

					const eventsCell = document.createElement('td');
					eventsCell.textContent = item.events.join(", ");
					row.appendChild(eventsCell);

					const eventscountCell = document.createElement('td');
					eventscountCell.textContent = item.events_count;
					row.appendChild(eventscountCell);

					if (item.events_count != item.events.length) {
						row.style = "border: 3px solid darkred; background-color:#fd8c59";
						Swal.fire({
							icon: 'warning',
							title: 'Просмотр отметок',
							html: 'Обнаружено <span style="color:#fd8c59">несоответствие</span> в кол-ве событий. Вероятно, кто-то отметил одно событие два раза. Пожалуйста, проверьте правильность выставления отметок'
						});
					}
					tbody.appendChild(row);
				});
			}
			table.appendChild(tbody);
			markedDiv.appendChild(table);
		})
		.catch(error => {
			console.log(error);
			Swal.fire({
				title: 'Ошибка',
				text: 'Ошибка получения данных',
				icon: 'error'
			});
		});
}
function formatDate(date) {
	const day = String(date.getDate()).padStart(2, '0');
	const month = String(date.getMonth() + 1).padStart(2, '0');
	const year = date.getFullYear();
	return `${year}.${month}.${day}`;
}
const calendar = document.querySelector('.calendar-line');
let calendar_data = null;
async function onResize() {
	try {
		if (calendar_data === null) {
			calendar_data = await fetchData("/api/line_calendar_demandpage");
			calendar_data.sort((a, b) => a.eventid - b.eventid);
		}
		updateCalendar(calendar, calendar_data);
	} catch {
		alert("Произошла неизвестная ошибка");
	}
}
function calendar_button(date, event) {
	document.getElementById("mark_table_datepickr").value = date.replace(/\./g, '-');
	//	document.getElementById("marked").scrollIntoView({ behavior: 'smooth' });
	loadMarkedUsersTable(date);
}