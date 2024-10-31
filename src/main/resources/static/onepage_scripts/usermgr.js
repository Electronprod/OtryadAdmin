if (window.location.href.includes("added_user")) {
	showNotification("Готово", "Пользователь успешно создан.", "success");
}
if (window.location.href.includes("edited_user")) {
	showNotification("Отредактировано", "Пользователь успешно отредактирован", "success");
}
const savepass = "Оставить старый";
function showAddUser() {
	document.getElementById('login').value = "";
	document.getElementById('password').value = "";
	document.getElementById('role').value = "";
	document.getElementById('telegram').value = "";
	document.getElementById('vkid').value = "";
	var oldButton = document.getElementById('editbtn');
	var newButton = document.getElementById('addbtn');
	oldButton.style.display = 'none';
	newButton.style.display = 'inline-block';
	showModal();
}
async function addUser() {
	const login = document.getElementById('login').value;
	const password = document.getElementById('password').value;
	const role = document.getElementById('role').value;
	const telegram = document.getElementById('telegram').value;
	const vkid = document.getElementById('vkid').value;

	if (!login || !password || !role) {
		alert('Пожалуйста, заполните обязательные поля: Логин, пароль, роль.');
		return;
	}
	const success = await sendPostData("/admin/usermgr/add?login=" + login + "&password=" + password + "&role=" + role + "&telegram=" + telegram + "&vkid=" + vkid);
	if (success) {
		window.location.href = window.location.href + "?added_user"
	}
	closeModal();
}
async function edit(id) {
	var oldButton = document.getElementById('addbtn');
	var newButton = document.getElementById('editbtn');
	oldButton.style.display = 'none';
	newButton.style.display = 'inline-block';
	const data = await fetchData("/admin/usermgr/edit?id=" + id);
	if (data == null) {
		console.log("Error editing: unable to get data from server.");
		return;
	}
	document.getElementById('userid').value = id;
	document.getElementById('login').value = data.login;
	document.getElementById('password').value = savepass;
	document.getElementById('role').value = data.role;
	document.getElementById('telegram').value = data.telegram;
	document.getElementById('vkid').value = data.vkid;
	showModal();
}
async function editUser() {
	const login = document.getElementById('login').value;
	let password = document.getElementById('password').value;
	const role = document.getElementById('role').value;
	const telegram = document.getElementById('telegram').value;
	const vkid = document.getElementById('vkid').value;
	const userid = document.getElementById('userid').value;
	if (!login || !password || !role || !userid) {
		alert('Пожалуйста, заполните обязательные поля.');
		return;
	}
	if (password == savepass) {
		password = "not_changed";
	}
	const success = await sendPostData("/admin/usermgr/edit?login=" + login + "&password=" + password + "&role=" + role + "&telegram=" + telegram + "&vkid=" + vkid + "&id=" + userid);
	if (success) {
		window.location.href = "/admin/usermgr?edited_user"
	}
	closeModal();
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
		const success = await sendPostData("/admin/usermgr/delete?id=" + id);
		if (success) {
			removeLine(id);
			showNotification("Удалено", "Пользователь успешно удален.", "success");
		}
	}
}