if (window.location.href.includes("added_human")) {
	showNotification("Успех!", "Человек добавлен", "success");
}
if (window.location.href.includes("edited_human")) {
	showNotification("Готово", "Человек отредактирован", "info");
}
if (window.location.href.includes("deleted_all")) {
	showNotification("Злодеяние свершилось", "Все люди были отправлены на перерождение.", "success");
}
function addHumanAction() {
	var oldButton = document.getElementById('edithuman-btn');
	var newButton = document.getElementById('addhuman-btn');

	oldButton.style.display = 'none';
	newButton.style.display = 'inline-block';
	document.getElementById('name').value = "";
	document.getElementById('lastname').value = "";
	document.getElementById('surname').value = "";
	document.getElementById('birthday').value = "";
	document.getElementById('school').value = "";
	document.getElementById('classnum').value = "";
	document.getElementById('phone').value = "";
	showModal();
}
async function addHuman() {
	const name = document.getElementById('name').value;
	const lastname = document.getElementById('lastname').value;
	const surname = document.getElementById('surname').value;
	const birthday = document.getElementById('birthday').value;
	const school = document.getElementById('school').value;
	const classnum = document.getElementById('classnum').value;
	const phone = document.getElementById('phone').value;
	const squad = document.getElementById('squad').value;

	if (!name || !lastname || !surname || !birthday || !school || !classnum || !phone || !squad) {
		alert('Пожалуйста, заполните обязательные поля.');
		return;
	}
	const success = await sendPostData("/admin/humanmgr/add?name=" + name + "&lastname=" + lastname + "&surname=" + surname + "&birthday=" + birthday + "&school=" + school + "&classnum=" + classnum + "&phone=" + phone + "&squad=" + squad);
	if (success) {
		window.location.href = "/admin/humanmgr?added_human"
	}
	closeModal();
}
async function editHuman() {
	const name = document.getElementById('name').value;
	const lastname = document.getElementById('lastname').value;
	const surname = document.getElementById('surname').value;
	const birthday = document.getElementById('birthday').value;
	const school = document.getElementById('school').value;
	const classnum = document.getElementById('classnum').value;
	const phone = document.getElementById('phone').value;
	const squad = document.getElementById('squad').value;
	const hid = document.getElementById('humanid').value;

	if (!name || !lastname || !surname || !birthday || !school || !classnum || !phone || !squad || !hid) {
		alert('Пожалуйста, заполните обязательные поля.');
		return;
	}
	const success = await sendPostData("/admin/humanmgr/edit?id=" + hid + "&name=" + name + "&lastname=" + lastname + "&surname=" + surname + "&birthday=" + birthday + "&school=" + school + "&classnum=" + classnum + "&phone=" + phone + "&squad=" + squad);
	if (success) {
		window.location.href = "/admin/humanmgr?edited_human"
	}
	closeModal();
}
async function edit(id) {
	var newButton = document.getElementById('edithuman-btn');
	var oldButton = document.getElementById('addhuman-btn');

	oldButton.style.display = 'none';
	newButton.style.display = 'inline-block';
	const data = await fetchData("/admin/humanmgr/edit?id=" + id);
	if (data == null) {
		console.log("Error editing: unable to get data from server.");
		return;
	}
	document.getElementById('humanid').value = id;
	document.getElementById('name').value = data.name;
	document.getElementById('lastname').value = data.lastname;
	document.getElementById('surname').value = data.surname;
	document.getElementById('birthday').value = data.birthday;
	document.getElementById('school').value = data.school;
	document.getElementById('classnum').value = data.classnum;
	document.getElementById('phone').value = data.phone;
	document.getElementById('squad').value = data.squad;
	showModal();
}
async function remove(id) {
	const result = await swal.fire({
		title: 'Вы уверены?',
		text: "Это действие нельзя будет отменить!",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		confirmButtonText: 'Да, удалить!',
		cancelButtonText: 'Отмена'
	});
	if (result.isConfirmed) {
		const success = await sendPostData("/admin/humanmgr/delete?id=" + id);
		if (success) {
			removeLine(id);
			showNotification("Готово!", "Человек удален.", "success");
		}
	}
}
async function deleteAll() {
	const result = await swal.fire({
		title: 'Вы уверены?',
		text: "Это действие нельзя будет отменить!",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		confirmButtonText: 'Да, удалить ВСЕ!',
		cancelButtonText: 'Отмена'
	});
	if (result.isConfirmed) {
		const success = await sendPostData("/admin/humanmgr/deleteall");
	}
}

function addList() {
	const csrfParam = document.querySelector('input[name="_csrf"]').getAttribute('name'); // Получаем название CSRF параметра
	const csrfToken = document.querySelector('input[name="_csrf"]').value; // Получаем значение CSRF токена

	Swal.fire({
		title: "Загрузите файл",
		icon: "info",
		showConfirmButton: false,
		showCancelButton: true,
		cancelButtonText: "Отмена",
		cancelButtonColor: "#d33",
		html: `
	        <p><strong>Эта функция добавляет новых людей</strong></p><br>
	        <p><b>Формат файла должен соответствовать шаблону:</b><br>squad_id;Фамилия;Имя;Отчество;Дата рождения;Школа;Класс;Номер телефона<br></p>
	        <br>
	        <form id="uploadForm" method="post" action="/admin/humanmgr/addlist" enctype="multipart/form-data">
	            <input type="hidden" name="${csrfParam}" value="${csrfToken}"><!-- CSRF токен -->
	            <input type="file" name="file" accept=".csv" required>
	            <button type="submit">Загрузить</button>
	        </form>
	        `,
		didRender: () => {
			document.getElementById("uploadForm").onsubmit = function(event) {
				// Прекращаем стандартное поведение, чтобы не было дополнительных обработчиков событий
				event.preventDefault();
				// Здесь можно добавить функции для обработки загрузки (например, AJAX) если нужно
				this.submit();
			};
		}
	});
}