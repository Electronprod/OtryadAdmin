function switchView(viewName) {
	document.querySelectorAll('.view-section').forEach(el => el.classList.remove('active'));
	document.getElementById(`view-${viewName}`).classList.add('active');
}
const video = document.getElementById('videoElement');
const canvas = document.getElementById('canvas');
const capturedImage = document.getElementById('capturedImage');
const placeholderContent = document.getElementById('placeholderContent');
const base64Input = document.getElementById('base64Output');
const cameraSelect = document.getElementById('cameraSource');
let stream = null;

async function getCameraSelection() {
	try {
		const devices = await navigator.mediaDevices.enumerateDevices();
		const videoDevices = devices.filter(device => device.kind === 'videoinput');
		const currentObj = cameraSelect.value;
		cameraSelect.innerHTML = '';

		if (videoDevices.length === 0) {
			const option = document.createElement('option');
			option.text = "Камера не найдена";
			cameraSelect.appendChild(option);
			return;
		}

		videoDevices.forEach((device, index) => {
			const option = document.createElement('option');
			option.value = device.deviceId;
			// Если label пустой (нет прав), пишем "Камера N"
			option.text = device.label || `Камера ${index + 1}`;
			cameraSelect.appendChild(option);
		});
		if (currentObj && videoDevices.some(d => d.deviceId === currentObj)) {
			cameraSelect.value = currentObj;
		} else {
			cameraSelect.value = videoDevices[0].deviceId;
		}
	} catch (err) {
		console.error("Ошибка при поиске устройств:", err);
	}
}

async function startCamera() {
	if (stream) {
		stream.getTracks().forEach(track => track.stop());
	}
	const videoSource = cameraSelect.value;
	const constraints = {
		video: {
			deviceId: videoSource ? { exact: videoSource } : undefined,
			width: { ideal: 1280 },
			height: { ideal: 720 }
		}
	};
	try {
		stream = await navigator.mediaDevices.getUserMedia(constraints);
		video.srcObject = stream;
		video.classList.remove('hidden');
		capturedImage.classList.add('hidden');
		placeholderContent.classList.add('hidden');
		getCameraSelection();
	} catch (err) {
		alert("Ошибка доступа к камере: " + err);
		console.error(err);
	}
}

// 3. Запускаем поиск камер при загрузке страницы
window.addEventListener('load', getCameraSelection);

// Слушаем подключение/отключение камер в реальном времени
navigator.mediaDevices.addEventListener('devicechange', getCameraSelection);

function takePhoto() {
	if (!stream) {
		alert("Сначала включите предпросмотр!");
		return;
	}
	canvas.width = video.videoWidth;
	canvas.height = video.videoHeight;
	const context = canvas.getContext('2d');
	context.drawImage(video, 0, 0, canvas.width, canvas.height);
	// Конвертация в Base64
	const dataURL = canvas.toDataURL('image/png');
	// Показываем фото
	capturedImage.src = dataURL;
	capturedImage.classList.remove('hidden');
	video.classList.add('hidden');
	// Сохраняем в скрытый инпут
	base64Input.value = dataURL;
	stream.getTracks().forEach(track => track.stop());
}

async function saveData() {
	const data = {
		surname: document.getElementById('surname').value,
		firstname: document.getElementById('firstname').value,
		patronymic: document.getElementById('patronymic').value,
		dob: document.getElementById('dob').value,
		school: document.getElementById('school').value,
		grade: document.getElementById('grade').value,
		address: document.getElementById('address').value,
		parent: document.getElementById('parent').value,
		phone: document.getElementById('phone').value,
		notes: document.getElementById('notes').value,
		photo: base64Input.value || null
	};
	if (!surname || !firstname || !patronymic || !dob || !school || !grade || !address || !parent || !phone) {
		alert("Пожалуйста, заполните все поля");
		return;
	}
	if ((await sendToServer("/head/collector/add", data)) != null) {
		alert('Данные успешно сохранены!');
		document.getElementById('studentForm').reset();
		resetCameraUI();
	}
}
async function sendToServer(address, bodyData) {
	try {
		const csrfToken = document.querySelector('input[name="_csrf"]').value;
		if (!csrfToken) {
			alert('Ошибка: Не найден токен CSRF');
			return null;
		}
		const response = await fetch(address, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'X-CSRF-TOKEN': csrfToken,
			},
			body: JSON.stringify(bodyData),
		});
		if (!response.ok) {
			let a = await response.json();
			console.log("Error message: " + JSON.stringify(a));
			if (JSON.stringify(a).includes("message") && JSON.stringify(a).includes("result") && JSON.stringify(a).includes("fail")) {
				alert("Ошибка HTTP " + response.status + "\nНекорректный ответ от сервера: " + a.message);
			} else {
				alert("Ошибка HTTP " + response.status);
			}
			return null;
		}
		const data = await response.json();
		return data;
	} catch (error) {
		console.log("sendToServer: ", error);
		alert("Ошибка: " + error);
		return null;
	}
}

function resetCameraUI() {
	base64Input.value = '';
	capturedImage.src = '';
	capturedImage.classList.add('hidden');
	video.classList.add('hidden');
	placeholderContent.classList.remove('hidden');
	if (stream) stream.getTracks().forEach(t => t.stop());
	stream = null;
}

async function deleteUser(id) {
	if (confirm('Удалить запись?')) {
		if ((await sendToServer("/head/collector/remove", id)) != null) {
			var divsToDelete = document.querySelectorAll('tr[name="' + id + '"]');
			divsToDelete.forEach(function(div) {
				div.remove();
			});
			alert("Удалено.")
		}
	}
}

// --- Профиль ---
async function viewProfile(id) {
	const student = await sendToServer("/head/collector/profile", id);
	if (!student) return;

	const container = document.getElementById('profileContent');
	const photoSrc = student.photo ? student.photo : '/icon.png';

	container.innerHTML = `
        <div class="profile-header">
            <img src="${photoSrc}" class="profile-photo-large">
            <div class="profile-name">
                <h1>${student.surname} ${student.firstname} ${student.patronymic}</h1>
                <div class="profile-meta">${student.school}, Класс ${student.grade}</div>
            </div>
        </div>
        
        <div class="profile-details">
            <div class="detail-item">
                <strong>Дата рождения</strong>
                <span>${student.dob || '-'}</span>
            </div>
            <div class="detail-item">
                <strong>Телефон</strong>
                <span>${student.phone || '-'}</span>
            </div>
            <div class="detail-item">
                <strong>Адрес</strong>
                <span>${student.address || '-'}</span>
            </div>
            <div class="detail-item">
                <strong>Представитель</strong>
                <span>${student.parent || '-'}</span>
            </div>
        </div>

        <div style="margin-top: 30px;">
            <h3 style="margin-bottom: 10px; color: var(--text-secondary); font-size: 0.9rem; text-transform: uppercase;">Дополнительно</h3>
            <div style="background: #f8f9fa; padding: 15px; border-radius: 4px; border: 1px solid var(--border-color);">
                ${student.notes || 'Нет примечаний'}
            </div>
        </div>
    `;
	switchView('profile');
}

async function exportTable() {
	let fileinfo = await sendToServer("/head/collector/export");
	const workbook = new ExcelJS.Workbook();
	const worksheet = workbook.addWorksheet('Candidates');
	worksheet.columns = [
		{ header: "Фото", key: "photo", width: 20 },
		{ header: "Фамилия", key: "surname", width: 18 },
		{ header: "Имя", key: "firstname", width: 18 },
		{ header: "Отчество", key: "patronymic", width: 18 },
		{ header: "ДР", key: "dob", width: 12 },
		{ header: "Школа", key: "school", width: 20 },
		{ header: "Класс", key: "grade", width: 10 },
		{ header: "Адрес", key: "address", width: 22 },
		{ header: "Представитель", key: "parent", width: 18 },
		{ header: "Телефон", key: "phone", width: 16 },
		{ header: "Дополнительно", key: "notes", width: 26 }
	];
	worksheet.getRow(1).font = { bold: true };
	worksheet.getRow(1).alignment = { vertical: 'middle', horizontal: 'center' };
	for (let i = 0; i < fileinfo.length; i++) {
		const human = fileinfo[i];
		const rowIndex = i + 2;

		const row = worksheet.addRow({
			surname: human.surname ?? "",
			firstname: human.firstname ?? "",
			patronymic: human.patronymic ?? "",
			dob: human.dob ?? "",
			school: human.school ?? "",
			grade: human.grade ?? "",
			address: human.address ?? "",
			parent: human.parent ?? "",
			phone: human.phone ?? "",
			notes: human.notes ?? ""
		});
		row.height = 80;
		row.alignment = { vertical: 'middle', horizontal: 'left' };
		if (human.photo && human.photo.includes("base64")) {
			try {
				const imageId = workbook.addImage({
					base64: human.photo,
					extension: 'png',
				});
				worksheet.addImage(imageId, {
					tl: { col: 0, row: rowIndex - 1 },
					br: { col: 1, row: rowIndex },
					editAs: 'oneCell'
				});
			} catch (err) {
				console.error("Ошибка вставки фото:", err);
			}
		}
	}
	const buffer = await workbook.xlsx.writeBuffer();
	const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
	const url = window.URL.createObjectURL(blob);
	const a = document.createElement('a');
	a.href = url;
	a.download = 'candidates.xlsx';
	a.click();
	window.URL.revokeObjectURL(url);
}