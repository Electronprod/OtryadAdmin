let counter = 0;
let showedAll = false;
async function fetchAndAppendStats(number) {
	if (showedAll) {
		return;
	}
	showLoader();
	const response = await fetch('/admin/statsmgr/data?number=' + number);
	if (!response.ok) {
		console.error('Error fetching stats!');
		alert("Ошибка подгрузки данных!");
		hideLoader();
		return;
	}
	const data = await response.json();
	const tbody = document.getElementById('stats-tbody');
	if (data.length == 0) {
		console.log("Showed all rows from DB.");
		showedAll = true;
		hideLoader();
		return;
	}
	data.forEach(stats => {
		const tr = document.createElement('tr');
		tr.setAttribute('name', stats.id);
		tr.innerHTML = `
        <td>${stats.date ?? ''}</td>
        <td>${stats.lastname ?? ''}</td>
        <td>${stats.name ?? ''}</td>
        <td>${stats.type ?? ''}</td>
        <td>${stats.isPresent ?? ''}</td>
        <td>${stats.reason ?? ''}</td>
        <td>${stats.author ?? ''}</td>
        <td>${stats.group ?? ''}</td>
        <td>${stats.event_id ?? ''}</td>
        <td><div class="action-buttons">
								<button class="button edit-btn" onclick="editRecord(${stats.id})">Редактировать</button>
								<button class="button delete-btn" onclick="removeRecord(${stats.id})">Удалить</button>
							</div></td>
    `;
		tbody.appendChild(tr);
	});
	counter += 1;
	main();
	hideLoader();
}
init_custom_option("type", "Свое событие...");
init_custom_option("reason", "Своя причина...");
document.addEventListener("DOMContentLoaded", () => {
	let isScrolling = false;
	const observer = new IntersectionObserver((entries) => {
		if (entries[0].isIntersecting && !isScrolling) {
			isScrolling = true;
			fetchAndAppendStats(counter).then(() => {
				isScrolling = false;
			});
		}
	}, {
		rootMargin: '200px'
	});
	observer.observe(document.getElementById('scroll-anchor'));
});
async function removeRecord(id) {
	const result = await swal.fire({
		title: 'Вы уверены?',
		text: "Это действие приведет к удалению только этой отметки.",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		confirmButtonText: 'Да, удалить!',
		cancelButtonText: 'Отмена'
	});
	if (result.isConfirmed) {
		const success = await sendPostData("/admin/statsmgr/delete?id=" + id);
		if (success) {
			removeLine(id);
			showNotification("Готово!", "Отметка удалена.", "success");
		}
	}
}
let editID;
async function editRecord(id) {
	editID = id;
	const data = await fetchData("/admin/statsmgr/edit?id=" + id);
	if (data == null) {
		console.log("Error editing: unable to get data from server.");
		alert('Error editing: unable to get data from server.');
		return;
	}
	document.getElementById('date-picker').value = data.date;
	document.getElementById('visit').value = data.isPresent;
	document.getElementById('author').value = data.author;
	document.getElementById('type').value = data.type;
	document.getElementById('reason').value = data.reason;
	if (document.getElementById('type').value == '')
		addNativeOption('type', data.type);
	if (document.getElementById('reason').value == '')
		addNativeOption('reason', data.reason);
	showModal();
}
async function editStatsRecord() {
	const date = document.getElementById('date-picker').value;
	const visit = document.getElementById('visit').value;
	const author = document.getElementById('author').value;
	const type = document.getElementById('type').value;
	const reason = document.getElementById('reason').value;
	if (!date || !visit || !author || !type || !reason) {
		alert('Пожалуйста, заполните обязательные поля.');
		return;
	}
	let success = await sendPostData("/admin/statsmgr/edit?id=" + editID + "&date=" + date + "&visit=" + visit + "&type=" + type + "&author=" + author + "&reason=" + reason);
	if (success) {
		let row = document.querySelector(`tr[name="${editID}"]`);
		if (row) {
			row.children[0].textContent = date ?? '';
			row.children[3].textContent = type ?? '';
			row.children[4].textContent = visit ?? '';
			row.children[5].textContent = reason ?? '';
			row.children[6].textContent = author ?? '';
			showNotification("Готово!", "Запись успешно отредактирована", "success");
			main();
		} else {
			showNotification("Выполнено с ошибкой", "Редактирование выполнено, но отобразить это не получилось. Перезагрузите страницу.", "warning");
		}
	}
	closeModal();
}
async function addNativeOption(selectorId, value) {
	var select = document.getElementById(selectorId);
	var customOption = document.createElement("option");
	customOption.value = value;
	customOption.textContent = value;
	select.appendChild(customOption);
	select.value = value;
}
async function editEvent(url, inputID) {
	let event = document.getElementById("eventid-field").value;
	let value = document.getElementById(inputID).value;
	if (!event) {
		alert("Событие не выбрано! Введите EventID!")
		return;
	}
	let success = await sendPostData(url + "?eventid=" + event + "&value=" + value);
	if (success) {
		Swal.fire({
			title: 'Дествие выполнено!',
			html: 'Для отображения результата <b>необходимо перезагрузить страницу</b>. Сделать это сейчас?',
			icon: 'success',
			showCancelButton: true,
			confirmButtonText: 'Да, перезагрузить',
			cancelButtonText: 'Нет'
		}).then((result) => {
			if (result.isConfirmed) {
				location.reload();
			}
		});
	}
}
async function removeEvent() {
	var id = prompt("Введите EventID:");
	if (!id)
		return;
	const result = await swal.fire({
		title: 'Вы уверены?',
		text: "Это действие приведет к удалению всех отметок, относящихся к этому событию.",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		confirmButtonText: 'Да, удалить!',
		cancelButtonText: 'Отмена'
	});
	if (result.isConfirmed) {
		const success = await sendPostData("/admin/statsmgr/delete_event?id=" + id);
		if (success) {
			Swal.fire({
				title: 'Дествие выполнено!',
				html: 'Для отображения результата <b>необходимо перезагрузить страницу</b>. Сделать это сейчас?',
				icon: 'success',
				showCancelButton: true,
				confirmButtonText: 'Да, перезагрузить',
				cancelButtonText: 'Нет'
			}).then((result) => {
				if (result.isConfirmed) {
					location.reload();
				}
			});
		}
	}
}