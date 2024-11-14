const groupID = document.getElementById('groupid').value;

async function addGroup(humanID) {
	const success = await sendPostData("/admin/groupmgr/manage/add?id=" + groupID + "&humanid=" + humanID);
	if (success) {
		removeDiv(humanID);
		showShortNotification("Добавлен.", "Человек добавлен в группу.", "success");
	}
}
async function removeGroup(humanID) {
	const success = await sendPostData("/admin/groupmgr/manage/delete?id=" + groupID + "&humanid=" + humanID);
	if (success) {
		removeDiv(humanID);
		showShortNotification("Удален.", "Человек удален из группы.", "success");
	}
}
function removeDiv(id) {
	var divsToDelete = document.querySelectorAll('div[name="' + id + '"]');
	divsToDelete.forEach(function(div) {
		div.remove();
	});
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