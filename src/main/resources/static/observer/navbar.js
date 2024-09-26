var csrfToken = document.querySelector('input[name="_csrf"]').value;
document.addEventListener('DOMContentLoaded', function() {
	const pages = [
		{ name: 'Главная', url: '/observer' },
		{ name: 'Статистика', url: '/observer/stats' },
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
            overflow: hidden;
            padding: 0 20px;
        }

        #navbar img {
            height: 40px;
            margin-right: 20px;
        }

        #navbar a {
            color: white;
            text-align: center;
            padding: 14px 20px;
            text-decoration: none;
            flex-grow: 1;
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
            padding: 10px 20px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            transition-duration: 0.4s;
        }

        #logoutForm button:hover {
            background-color: white;
            color: black;
            border: 2px solid #f44336;
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
