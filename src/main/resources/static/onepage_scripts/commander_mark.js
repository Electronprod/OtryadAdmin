document.getElementById("dateField").valueAsDate = new Date()
async function fetchData(url) {
	try {
		const response = await fetch(url);
		if (!response.ok) {
			throw new Error(`Ошибка HTTP: ${response.status}`);
		}
		return await response.json();
	} catch (error) {
		console.error('Ошибка при получении данных:', error);
		Swal.fire({
			title: "Ошибка!",
			text: "Ошибка при получении информации. Пожалуйста, перезагрузите страницу. \nОшибка: " + error,
			icon: "error"
		});
		return "";
	}
}
function submitData() {
	Swal.fire({
		title: 'Вы уверены?',
		text: "Вы точно выбрали всех ребят? \n Вы ввели правильное название события?",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		confirmButtonText: 'Да, отправить',
		cancelButtonText: 'Отмена'
	}).then((result) => {
		if (result.isConfirmed) {
			sendData();
		}
	});
}

async function sendData() {
	const uncheckedPeople = [];
	// Проходим по всем чекбоксам
	const checkboxes = document.querySelectorAll('.custom-checkbox');
	checkboxes.forEach(checkbox => {
		if (checkbox.checked) {
			const row = checkbox.closest('tr');
			uncheckedPeople.push(
				checkbox.value
			);
		}
	});
	var selectElement = document.getElementById('statsType');
	if (selectElement.value == "") {
		Swal.fire({
			title: "Ошибка заполнения!",
			text: "Вы не ввели название события",
			icon: "error"
		});
		return;
	}
	const bodyData = {
		checkedPeople: uncheckedPeople,
		date: document.getElementById("dateField").value,
		eventName: selectElement.value
	};
	var csrfToken = document.querySelector('input[name="_csrf"]').value;
	// Отправляем данные на сервер
	try {
		const response = await fetch('/commander/mark', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'X-CSRF-TOKEN': csrfToken,
			},
			body: JSON.stringify(bodyData)
		});

		if (!response.ok) {
			console.log("HTTP error: " + response);
			Swal.fire({
				title: "Провал!",
				text: `Ошибка HTTP: ${response.status}\nНекорректный ответ от сервера.`,
				icon: "error"
			});
			return false;
		}

		const data = await response.json();
		console.log('mark success:', data);
		Swal.fire({
			title: "Успех!",
			text: "Ребята отмечены",
			icon: "success"
		});
		return true;
	} catch (error) {
		console.error('mark error:', error);
		Swal.fire({
			title: "Провал!",
			text: `Ошибка при выставлении отметок\n${error}`,
			icon: "error"
		});
		return false;
	}
}