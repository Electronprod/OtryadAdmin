if (window.location.href.includes("saved")) {
	Swal.fire({
		title: "УСПЕХ!",
		text: "Сохранено.",
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
if (window.location.href.includes("sent")) {
	Swal.fire({
		title: "УСПЕХ!",
		text: "Данные отправлены",
		icon: "success"
	});
}