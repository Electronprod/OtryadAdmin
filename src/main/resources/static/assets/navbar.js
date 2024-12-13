document.addEventListener('DOMContentLoaded', function() {
	var csrfToken = document.querySelector('input[name="_csrf"]').value;
	let pages = [
		{ name: 'Error', url: '/' }
	];
	if (window.location.href.toLowerCase().includes("/observer")) {
		pages = [
			{ name: 'Главная', url: '/observer/stats' },
			{ name: 'Список отряда', url: '/observer/data' }
		];
	} else if (window.location.href.toLowerCase().includes("/squadcommander")) {
		pages = [
			{ name: 'Главная', url: '/squadcommander/mark' },
			{ name: 'Статистика', url: '/squadcommander/stats' },
			{ name: 'Ребята', url: '/squadcommander/humans' }
		];
	} else if (window.location.href.toLowerCase().includes("/commander")) {
		pages = [
			{ name: 'Главная', url: '/commander' },
			{ name: 'Статистика', url: '/commander/stats' },
			{ name: 'Список отряда', url: '/commander/humans' }
		];
	} else if (window.location.href.toLowerCase().includes("/admin")) {
		pages = [
			{ name: 'Главная', url: '/admin' },
			{ name: 'Пользователи', url: '/admin/usermgr' },
			{ name: 'Звенья', url: '/admin/squadmgr' },
			{ name: 'Люди', url: '/admin/humanmgr' },
			{ name: 'Группы', url: '/admin/groupmgr' },
			{ name: 'Статистика', url: '/admin/statsmgr' }
		];
	}

	let navbar = document.getElementById('navbar');
	if (!navbar) {
		navbar = document.createElement('div');
		navbar.id = 'navbar';
		document.body.insertBefore(navbar, document.body.firstChild);
	}
	const style = document.createElement('style');
	style.textContent = `
#navbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: #333;
    padding: 0 1.25rem;
    flex-wrap: wrap;
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
    flex-grow: 1;
    transition: background-color 0.3s, color 0.3s;
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
    font-size: 1rem;
    margin: 0.25rem 0.125rem;
    transition-duration: 0.4s;
}

#logoutForm button:hover {
    background-color: white;
    color: black;
    border: 0.125rem solid #f44336;
}

@media (max-width: 768px) {
  #navbar img{
    display: none;
  }
    #navbar a {
        flex-grow: unset;
        width: 100%;
    }

    #logoutForm {
        width: 100%;
        text-align: center;
    }

    #logoutForm button {
        width: 100%;
        box-sizing: border-box;
    }
}
  `;
	document.head.appendChild(style);
	// Adding site icon dynamically
	const icon = document.createElement('img');
	icon.src = '/icon.png';
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
	// Adding CSRF token
	const csrfTokenInput = document.createElement('input');
	csrfTokenInput.type = 'hidden';
	csrfTokenInput.name = '_csrf';
	csrfTokenInput.value = csrfToken;
	logoutForm.appendChild(csrfTokenInput);
	const logoutButton = document.createElement('button');
	logoutButton.type = 'submit';
	logoutButton.textContent = 'Выйти';
	logoutForm.appendChild(logoutButton);
	navbar.appendChild(logoutForm);
});

function loadScript(url, callback) {
	const script = document.createElement('script');
	script.src = url;
	script.type = 'text/javascript';
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