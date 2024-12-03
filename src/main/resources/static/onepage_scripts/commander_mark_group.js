// Initializing page
document.getElementById("dateField").valueAsDate = new Date();
const groupID = document.getElementById('groupid').value;
document.getElementById('selectAll').addEventListener('change', function() {
	const checkboxes = document.querySelectorAll('input[name="selectedIds"]');
	checkboxes.forEach((checkbox) => {
		checkbox.checked = this.checked;
		const index = checkbox.id.split('-')[1];
		const selectInput = document.getElementById('details-' + index);
		if (selectInput) {
			selectInput.disabled = checkbox.checked;
		}
	});
});
document.addEventListener('DOMContentLoaded', function() {
	const checkboxes = document.querySelectorAll('.custom-checkbox');

	checkboxes.forEach(checkbox => {
		checkbox.addEventListener('change', function() {
			const index = this.id.split('-')[1];
			const selectInput = document.getElementById('details-' + index);

			if (selectInput) {
				selectInput.disabled = this.checked;
			}
		});
	});
});
function loadScript(url, callback) {
	const script = document.createElement('script');
	script.src = url;
	script.type = 'text/javascript';
	//script.async = true;
	script.onload = function() {
		console.log(`Script ${url} connected successfully.`);
		if (callback) callback();
	};
	script.onerror = function() {
		console.error(`Error connecting script: ${url}.`);
		alert("Возникла критическая ошибка при загрузке страницы. Повторите попытку позже.");
	};
	document.body.appendChild(script);
}
// Loading other scripts
loadScript("/assets/loader_modal.js", null);
if ((typeof Swal === "function") == false) {
	loadScript("/public_resources/sweetalert2.js", null);
}

function submitData() {
	Swal.fire({
		title: 'Вы уверены?',
		text: "Подтвердите правильность данных, прежде чем отправлять их на сервер",
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
	showLoader();
	const uncheckedPeople = [];
	const checkedPeople = [];
	const checkboxes = document.querySelectorAll('.custom-checkbox');
	checkboxes.forEach(checkbox => {
		if (!checkbox.checked) {
			const row = checkbox.closest('tr');
			const reasonSelect = row ? row.querySelector('.details-input') : null;
			if (reasonSelect) {
				uncheckedPeople.push(JSON.stringify(
					{
						id: checkbox.value,
						reason: reasonSelect.value
					}
				));
			} else {
				console.warn(`Не удалось найти 'details-input' для элемента с id: ${checkbox.value}`);
			}
		} else {
			checkedPeople.push(checkbox.value);
		}
	});
	var selectElement = document.getElementById('statsType');
	var dateField = document.getElementById('dateField');
	const bodyData = {
		uncheckedPeople: uncheckedPeople,
		checkedPeople: checkedPeople,
		statsType: selectElement ? selectElement.value : null,
		date: dateField.value,
		group: groupID
	};
	if (!selectElement) {
		console.error('Element with id "statsType" not found.');
		Swal.fire({
			title: "Ошибка!",
			text: "Не удалось найти элемент для выбранного типа статистики. Пожалуйста, обновите страницу.",
			icon: "error"
		});
		hideLoader();
		return;
	}
	var csrfTokenInput = document.querySelector('input[name="_csrf"]');
	var csrfToken = csrfTokenInput ? csrfTokenInput.value : null;
	if (!csrfToken) {
		console.error('CSRF token input not found.');
		Swal.fire({
			title: "Ошибка!",
			text: "Не удалось найти CSRF токен. Пожалуйста, обновите страницу.",
			icon: "error"
		});
		hideLoader();
		return;
	}
	try {
		const response = await fetch('/commander/markgroup', {
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
			hideLoader();
			return false;
		}

		const data = await response.json();
		console.log('mark success:', data);
		Swal.fire({
			title: "Успех!",
			text: "Ребята отмечены",
			icon: "success"
		});
		hideLoader();
		return true;
	} catch (error) {
		console.error('mark error:', error);
		Swal.fire({
			title: "Провал!",
			text: `Ошибка при выставлении отметок\n${error}`,
			icon: "error"
		});
		hideLoader();
		return false;
	}
}