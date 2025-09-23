document.getElementById("mark_table_datepickr").valueAsDate = new Date()
document.getElementById('sendremind').addEventListener('submit',
	function(event) {
		event.preventDefault();
		sendData(this);
	});
document.getElementById('sendmessage').addEventListener('submit',
	function(event) {
		event.preventDefault();
		sendData(this);
	});
document.addEventListener("DOMContentLoaded", () => {
	sortTable(1, 'rating');
	sortTable(1, 'rating1');
	console.log("Sorted rating.");
	loadMarkedUsersTable(formatDate(new Date()));
});
function receiver_select(val) {
	if (val == -1) {
		document.getElementById('send_remaind_table_sq').style = 'display:true';
		document.getElementById('sendremaind_btn').style = 'display:none';
	} else {
		document.getElementById('send_remaind_table_sq').style = 'display:none';
		document.getElementById('sendremaind_btn').style = 'display:true';
	}
}
function sendRemind(val, id) {
	if (document.getElementById('sendremind_eventname').value == "") {
		showError('Введите название события!');
		return;
	}
	setSmall(true);
	var s = document.getElementById('remind_userid_selector');
	let oldVal = s.options[s.selectedIndex].value;
	s.options[s.selectedIndex].value = id;
	console.log("Change: " + oldVal + ">>" + s.options[s.selectedIndex].value);
	sendData(document.getElementById('sendremind'));
	s.options[s.selectedIndex].value = oldVal;
	console.log("Back to: " + s.options[s.selectedIndex].value);
	val.style = "background-color: darkred";
}
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
				document.getElementById('showStatsByDate').disabled=true;
			} else {
				document.getElementById('showStatsByDate').disabled=false;
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
							title: 'Обнаружено несоответствие!',
							html: 'Обнаружено <span style="color:#fd8c59">несоответствие</span> в кол-ве событий. Вероятно, кто-то отметил одно событие два раза. Пожалуйста, проверьте правильность выставления отметок'
						})
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