if (window.location.href.includes("saved")) {
	Swal.fire({
		title: "УСПЕХ!",
		text: "Сохранено.",
		icon: "success"
	});
}
if (window.location.href.includes("published")) {
	Swal.fire({
		title: "УСПЕХ!",
		text: "Опубликовано.",
		icon: "success"
	});
}
if (window.location.href.includes("error_usernotfound")) {
	Swal.fire({
		title: "ОШИБКА АВТОРИЗАЦИИ!",
		text: "Пользователь не найден в сессии.",
		icon: "error"
	});
}
if (window.location.href.includes("error_notfound")) {
	Swal.fire({
		title: "ОШИБКА!",
		text: "Обьект не найден в базе данных.",
		icon: "error"
	});
}
if (window.location.href.includes("server_incorrectreq")) {
	Swal.fire({
		title: "ОШИБКА!",
		text: "Запрос был составлен неверно.",
		icon: "error"
	});
}
if (window.location.href.includes("sent")) {
	Swal.fire({
		title: "УСПЕХ!",
		text: "Данные отправлены",
		icon: "success"
	});
}