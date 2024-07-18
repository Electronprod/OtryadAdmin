document.querySelector('form').addEventListener('submit', function(event) {
	const inputs = document.querySelectorAll('input');
	let isValid = true;

	inputs.forEach(function(input) {
		if (input.name !== 'telegram' && input.value.trim() === '') {
			isValid = false;
		}
	});

	if (!isValid) {
		Swal.fire({
			icon: "error",
			title: "Ошибка",
			text: "Все поля в форме должны быть заполнены.",
		});
		event.preventDefault();
	}
});