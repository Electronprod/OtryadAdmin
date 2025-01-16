const groupID = document.getElementById('groupid').value;

async function addGroup(humanID) {
	const success = await sendPostData("/admin/groupmgr/manage/add?id=" + groupID + "&humanid=" + humanID);
	if (success) {
		toggleButtons(humanID);
		showShortNotification("Добавлен.", "Человек добавлен в группу.", "success");
	}
}
async function removeGroup(humanID) {
	const success = await sendPostData("/admin/groupmgr/manage/delete?id=" + groupID + "&humanid=" + humanID);
	if (success) {
		toggleButtons(humanID);
		showShortNotification("Удален.", "Человек удален из группы.", "success");
	}
}
function toggleButtons(humanId) {
	const addButton = document.getElementById(`add-${humanId}`);
	const deleteButton = document.getElementById(`delete-${humanId}`);

	if (addButton && deleteButton) {
		if (addButton.style.display === 'none') {
			addButton.style.display = '';
			deleteButton.style.display = 'none';
		} else {
			addButton.style.display = 'none';
			deleteButton.style.display = '';
		}
	} else {
		console.error('Buttons not found. Check the IDs.');
	}
}
function showShortNotification(title, text, icon) {
	Swal.fire({
		title: title,
		text: text,
		icon: icon,
		toast: true,
		position: 'top-end',
		showConfirmButton: false,
		timer: 1000,
		timerProgressBar: true,
		showClass: {
			popup: 'swal2-show'
		},
		hideClass: {
			popup: 'swal2-hide'
		}
	});
}
async function remove(id) {
	const result = await
		swal.fire({
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
		const success = await
			sendPostData("/admin/groupmgr/delete?id=" + id);
		if (success) {
			removeLine(id);
			showNotification("Готово!", "Группа удалена.", "success");
		}
	}
}
async function changeStatus(btn, id) {
	const success = await sendPostData("/admin/groupmgr/change_editable?id=" + id);
	if (success) {
		showNotification("Статус группы обновлен", "Изменения отобразятся после перезагрузки страницы.", "success");
		btn.disabled = true;
	}
}