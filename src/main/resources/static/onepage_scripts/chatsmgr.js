let modal_chatID = -1;
async function connect(chatID) {
	modal_chatID = chatID;
	document.getElementById('chat_username').value = document.getElementById(`${chatID}-username`).textContent;
	showModal();
}

async function add() {
	let userid = document.getElementById('userid').value;
	const success = await sendPostData("/admin/chatsmgr/connect?id=" + modal_chatID + "&userid=" + userid);
	if (success) {
		showNotification("Пользователь привязан", "Теперь чат привязан к пользователю.", "success");
	}
	closeModal();
	await delay(2000);
	location.reload();
}

async function disconnect(chatID) {
	const success = await
		sendPostData("/admin/chatsmgr/disconnect?id=" + chatID);
	if (success) {
		removeLine(chatID);
		showNotification("Отвязано и Удалено", "Чат был отвязан от пользователя и удален.", "success");
	}
}

async function remove(id) {
	const success = await
		sendPostData("/admin/chatsmgr/delete?id=" + id);
	if (success) {
		removeLine(id);
		showNotification("Готово!", "Регистрация удалена", "success");
	}
}
function delay(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}