// Функция для получения данных с указанного URL
async function fetchData(url) {
	try {
		const response = await fetch(url);
		if (!response.ok) {
			throw new Error(`Ошибка HTTP: ${response.status}`);
		}
		return await response.json();
	} catch (error) {
		console.error('Ошибка при получении данных:', error);
		return null; // Возвращаем null в случае ошибки
	}
}

// Функция для замены текста в ячейках таблицы
function replaceText(replacements) {
	document.querySelectorAll('table td').forEach(function(td) {
		Object.keys(replacements).forEach(key => {
			const regex = new RegExp(key, 'g'); // Создаем регулярное выражение для замены
			td.textContent = td.textContent.replace(regex, replacements[key].replace(/\(.*?\)/g, '').trim());
		});
	});
}

// Пример URL для GET-запроса
const url = window.location.origin + '/api/getrenamerdata'; // Замените на нужный URL

// Основная функция для выполнения GET-запроса и замены текста
async function main() {
	if (window.location.href.includes("stop_renamer")) {
		console.log('Stopped renamer with url argument.');
		return;
	}
	const replacements = await fetchData(url);
	if (replacements) {
		replaceText(replacements);
	}
}
// Вызываем функцию при загрузке окна
window.onload = main;
