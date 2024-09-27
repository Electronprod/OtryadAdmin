var csrfToken = document.querySelector('input[name="_csrf"]').value;
document.addEventListener('DOMContentLoaded', function() {
	const pages = [
		//		{ name: 'Главная', url: '/observer' },
		{ name: 'Главная', url: '/observer/stats' },
		{ name: 'Список отряда', url: '/observer/data' }
	];

	const navbar = document.getElementById('navbar');

	// Adding styles dynamically
	const style = document.createElement('style');
	style.textContent = `
#navbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: #333;
    /* Используем padding в rem для адаптации к различным размерам шрифтов */
    padding: 0 1.25rem;
    flex-wrap: wrap; /* Позволяет элементам переходить на новую строку для узких экранов */
}

#navbar img {
    height: 2.5rem;
    margin-right: 1.25rem;
}

#navbar a {
    color: white;
    text-align: center;
    padding: 0.875rem 1.25rem;
    text-decoration: none;
    /* Flex-grow оставляем, чтобы ссылки равномерно занимали весь оставшийся доступный размер */
    flex-grow: 1;
    transition: background-color 0.3s, color 0.3s; /* Добавляем плавный переход для наведения */
}

#navbar a:hover {
    background-color: #ddd;
    color: black;
}

#logoutForm {
    margin-left: auto;
}

#logoutForm button {
    cursor: pointer;
    background-color: #f44336;
    color: white;
    border: none;
    padding: 0.625rem 1.25rem;
    text-align: center;
    text-decoration: none;
    display: inline-block;
    font-size: 1rem; /* Используем rem для соответствия размера шрифта */
    margin: 0.25rem 0.125rem;
    transition-duration: 0.4s;
}

#logoutForm button:hover {
    background-color: white;
    color: black;
    border: 0.125rem solid #f44336;
}

/* Медиа-запросы для адаптивности на небольших устройствах */
@media (max-width: 768px) {
	#navbar img{
		display: none;
	}
    #navbar a {
        flex-grow: unset;
        /* Задаем фиксированную ширину для урегулирования компоновки на меньших экранах */
        width: 100%;
    }

    #logoutForm {
        width: 100%;
        text-align: center; /* Переводим кнопку в центр для узких экранов */
    }

    #logoutForm button {
        width: 100%;
        box-sizing: border-box; /* Учитываем боксы для полной ширины */
    }
}
    `;

	document.head.appendChild(style);

	// Adding site icon dynamically
	const icon = document.createElement('img');
	icon.src = '/icon.png'; // Замените на путь к вашей иконке
	icon.alt = 'Site Icon';
	navbar.appendChild(icon);

	// Constructing navbar
	pages.forEach(page => {
		const link = document.createElement('a');
		link.href = page.url;
		link.textContent = page.name;
		navbar.appendChild(link);
	});

	// Adding logout form
	const logoutForm = document.createElement('form');
	logoutForm.id = 'logoutForm';
	logoutForm.method = 'POST';
	logoutForm.action = '/auth/logout';

	// Adding CSRF token if needed
	const csrfTokenInput = document.createElement('input');
	csrfTokenInput.type = 'hidden';
	csrfTokenInput.name = '_csrf'; // Замените на имя поля CSRF токена, если используется
	csrfTokenInput.value = csrfToken; // Замените на значение CSRF токена
	logoutForm.appendChild(csrfTokenInput);

	const logoutButton = document.createElement('button');
	logoutButton.type = 'submit';
	logoutButton.textContent = 'Выйти';
	logoutForm.appendChild(logoutButton);

	navbar.appendChild(logoutForm);
});
