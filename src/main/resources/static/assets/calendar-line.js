const calendarstyle = document.createElement('style');
calendarstyle.textContent = `
.calendar-line {
            display: flex;
            justify-content: space-around;
            padding: 5px;
            background-color: #f0f0f0;
            border-radius: 5px;
			position: relative;
        }
        .calendar-button {
            background-color: white;
            color: #000;
            border: 1px solid orange;
            padding: 10px;
            cursor: pointer;
            width: 70px;
            height: 40px;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 14px;
            font-weight: bold;
            transition: background-color 0.3s;
            margin: 5px;
        }
        .calendar-button:hover {
            background-color: #ddd;
        }
		.calendar-label {
		    position: relative;
		    bottom: -25px;
		    left: 43%;
		    transform: translateX(-50%);
		    width: 25px;
		    height: 15px;
		    background-color: orange;
		    color: white;
		    display: flex;
		    justify-content: center;
		    align-items: center;
		    font-size: 10px;
		    border-radius: 3px;
		}
		.green-font{
			color: green;
		}
		.red-font{
			color: red
		}
  `;
document.head.appendChild(calendarstyle);
const months = ["янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"];

function createButton(date) {
	const button = document.createElement('button');
	button.textContent = `${date.date.split('.')[2]} ${months[date.date.split('.')[1] - 1]}`;
	button.classList.add('calendar-button');
	const squareLabel = document.createElement('div');
	squareLabel.classList.add('calendar-label');
	squareLabel.innerHTML = `<span class="green-font">${date.present}</span>/<span class="red-font">${date.absent}</span>`;
	button.appendChild(squareLabel);
	const tooltip = createTooltip();
	tooltip.textContent = `${date.event}`;
	if (!('ontouchstart' in window || navigator.maxTouchPoints > 0)) {
		button.onmouseover = function(event) {
			document.body.appendChild(tooltip);
			tooltip.style.display = 'block';
			tooltip.style.left = `${event.pageX}px`;
			tooltip.style.top = `${event.pageY}px`;
		};
	}
	button.onmouseout = function() {
		tooltip.remove();
	};
	button.onclick = function() {
		tooltip.remove();
		calendar_button(date.date, date.eventid);
	};
	return button;
}

function updateCalendar(calendar, dates) {
	while (calendar.firstChild) {
		calendar.removeChild(calendar.firstChild);
	}
	const screenWidth = window.innerWidth;
	let visibleDates = 10;
	if (screenWidth < 1000) {
		visibleDates = 7;
	}
	if (screenWidth < 600) {
		visibleDates = 5;
	}
	if (screenWidth < 500) {
		visibleDates = 3;
	}
	for (let i = 0; i < visibleDates; i++) {
		try {
			const button = createButton(dates.slice(-visibleDates)[i]);
			calendar.appendChild(button);
		} catch (err) { }
	}
}
function createTooltip() {
	const tooltip = document.createElement('div');
	tooltip.className = 'tooltip';
	tooltip.style.position = 'absolute';
	tooltip.style.backgroundColor = '#333';
	tooltip.style.color = '#fff';
	tooltip.style.padding = '10px';
	tooltip.style.borderRadius = '5px';
	tooltip.style.display = 'none';
	tooltip.style.zIndex = '1000';
	return tooltip;
}
async function fetchData(url) {
	try {
		const response = await fetch(url);
		if (!response.ok) {
			alert("Ошибка HTTP " + response.status);
			return null;
		}
		return await response.json();
	} catch (error) {
		alert("Неизвестная ошибка: " + error);
		return null;
	}
}
function insertTable(table, data) {
	const tableContainer = document.getElementById(table);
	let tableHTML = '<table><thead><tr><th>Дата</th><th>Название</th><th>Участие</th></tr></thead><tbody>';
	data.forEach((row) => {
		tableHTML += `<tr style="cursor:pointer" onclick="calendar_button('${row.date}', ${row.eventid})"><td>${row.date}</td><td>${row.event}</td><td><span class="green-font">${row.present}</span>/<span class="red-font">${row.absent}</span></td></tr>`;
	});
	tableHTML += '</tbody></table>';
	tableContainer.innerHTML = tableHTML;
}
async function simValuesFromJSON(data, key) {
	let counter = 0;
	data.forEach((row) => {
		counter = counter + row[key];
	});
	return counter;
}
function genReasonsTable(data, table) {
	let reasonCount = {};
	const tableContainer = document.getElementById(table);
	data.forEach(item => {
		const reason = item.reason;
		if (reason) {
			if (reasonCount[reason]) {
				reasonCount[reason]++;
			} else {
				reasonCount[reason] = 1;
			}
		}
	});
	let tableHtml = '<table border="1"><tr><th>Причина</th><th>Пропуски</th></tr>';
	for (const [reason, count] of Object.entries(reasonCount)) {
		tableHtml += `<tr><td>${reason}</td><td>${count}</td></tr>`;
	}
	tableContainer.innerHTML = tableHtml;
}