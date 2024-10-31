if (window.location.href.includes("added_squad")) {
	showNotification("Успех", "Звено усмешно создано", "success");
}
if (window.location.href.includes("edited_squad")) {
	showNotification("Успех", "Звено усмешно отредактировано", "info");
}
async function addSquad() {
	const name = document.getElementById('name').value;
	const commandername = document.getElementById('commandername').value;
	const commander = document.getElementById('commander').value;

	if (!name || !commander || !commandername) {
		alert('Пожалуйста, заполните обязательные поля.');
		return;
	}
	const success = await sendPostData("/admin/squadmgr/add?user=" + commander + "&commandername=" + commandername + "&name=" + name);
	if (success) {
		window.location.href = "/admin/squadmgr?added_squad"
	}
	closeModal();
}
async function editSquad() {
	const name = document.getElementById('name').value;
	const commandername = document.getElementById('commandername').value;
	const commander = document.getElementById('commander').value;
	const sid = document.getElementById('squadid').value;
	if (!name || !commandername || !sid) {
		alert('Пожалуйста, заполните обязательные поля.');
		return;
	}
	let success = await sendPostData("/admin/squadmgr/edit?id=" + sid + "&commandername=" + commandername + "&name=" + name + "&user=" + commander);
	if (success) {
		window.location.href = "/admin/squadmgr?edited_squad"
	}
	closeModal();
}
function add() {
	var oldButton = document.getElementById('editbtn');
	var newButton = document.getElementById('addbtn');
	oldButton.style.display = 'none';
	newButton.style.display = 'inline-block';
	document.getElementById('name').value = "";
	document.getElementById('commandername').value = "";
	document.getElementById('commander').value = "";
	showModal();
}
async function edit(id) {
	var oldButton = document.getElementById('addbtn');
	var newButton = document.getElementById('editbtn');
	oldButton.style.display = 'none';
	newButton.style.display = 'inline-block';
	const data = await fetchData("/admin/squadmgr/edit?id=" + id);
	if (data == null) {
		console.log("Error editing: unable to get data from server.");
		return;
	}
	document.getElementById('squadid').value = id;
	document.getElementById('name').value = data.name;
	document.getElementById('commandername').value = data.commandername;
	const select = document.getElementById('commander');
	const newOption = document.createElement('option');
	newOption.text = 'Не менять';
	newOption.value = data.commander;
	select.appendChild(newOption);
	document.getElementById('commander').value = data.commander;
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
		const success = await sendPostData("/admin/squadmgr/delete?id=" + id);
		if (success) {
			removeLine(id);
			showNotification("Удалено", "Звено успешно удалено.", "success");
		}
	}
}