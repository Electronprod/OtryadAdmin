if (window.location.href.includes("added_squad")) {
	showNotification("Успех", "Звено усмешно создано", "success");
}
if (window.location.href.includes("edited_squad")) {
	showNotification("Успех", "Звено усмешно отредактировано", "info");
}
async function addSquad() {
	const name = document.getElementById('name').value;
	const commander = document.getElementById('commander').value;
	if (!name || !commander) {
		alert('Пожалуйста, заполните обязательные поля.');
		return;
	}
	const success = await sendPostData("/admin/squadmgr/add?user=" + commander + "&name=" + name);
	if (success) {
		window.location.href = "/admin/squadmgr?added_squad"
	}
	closeModal();
}
async function editSquad() {
	const name = document.getElementById('name').value;
	const sid = document.getElementById('squadid').value;
	if (!name || !sid) {
		alert('Пожалуйста, заполните обязательные поля.');
		return;
	}
	let success = await sendPostData("/admin/squadmgr/edit?id=" + sid + "&name=" + name);
	if (success) {
		window.location.href = "/admin/squadmgr?edited_squad"
	}
	closeModal();
}
function add() {
	document.getElementById('commander_edit').hidden = false;
	var oldButton = document.getElementById('editbtn');
	var newButton = document.getElementById('addbtn');
	oldButton.style.display = 'none';
	newButton.style.display = 'inline-block';
	document.getElementById('name').value = "";
	document.getElementById('commander').value = "";
	showModal();
}
async function edit(id) {
	document.getElementById('commander_edit').hidden = true;
	var oldButton = document.getElementById('addbtn');
	var newButton = document.getElementById('editbtn');
	oldButton.style.display = 'none';
	newButton.style.display = 'inline-block';
	const data = await fetchData("/admin/squadmgr/edit?id=" + id);
	if (data == null) {
		console.log("Error editing: unable to get data from server.");
		alert('Error editing: unable to get data from server.');
		return;
	}
	document.getElementById('squadid').value = id;
	document.getElementById('name').value = data.name;
	document.getElementById('commander').value = data.commander;
	showModal();
}
async function remove(id) {
	const result = await swal.fire({
		title: 'Вы уверены?',
		text: "Все люди из звена будут удалены тоже, как и их статистика!",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		confirmButtonText: 'Да, удалить!',
		cancelButtonText: 'Отмена'
	});
	if (result.isConfirmed) {
		const success = await sendPostData("/admin/squadmgr/delete?id=" + id);
		if (success) {
			removeLine(id);
			showNotification("Удалено", "Звено успешно удалено.", "success");
		}
	}
}