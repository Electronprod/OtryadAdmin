document.addEventListener('DOMContentLoaded', function () {
    const pages = [
        { name: 'Главная', url: '/admin' },
        { name: 'Пользователи', url: '/admin/usermgr' },
        { name: 'Звенья', url: '/admin/squadmgr' },
        { name: 'Люди', url: '/admin/humanmgr' }
    ];

    const navbar = document.getElementById('navbar');

    // Adding styles dynamically
    const style = document.createElement('style');
    style.textContent = `
        body {
            font-family: Arial, sans-serif;
            margin: 0; /* Убираем отступы */
        }
        
        #navbar {
            display: flex;
            align-items: center;
            background-color: #333;
            overflow: hidden;
            padding: 0 20px; /* Добавляем внутренние отступы */
        }

        #navbar img {
            height: 40px;
            margin-right: 20px; /* Расстояние между иконкой и ссылками */
        }

        #navbar a {
            color: white;
            text-align: center;
            padding: 14px 20px;
            text-decoration: none;
            flex-grow: 1; /* Распределяем ссылки равномерно */
        }

        #navbar a:hover {
            background-color: #ddd;
            color: black;
        }
    `;
    document.head.appendChild(style);

    // Adding site icon dynamically
    const icon = document.createElement('img');
    icon.src = '/logo.png'; // Замените на путь к вашей иконке
    icon.alt = 'Site Icon';
    navbar.appendChild(icon);

    // Constructing navbar
    pages.forEach(page => {
        const link = document.createElement('a');
        link.href = page.url;
        link.textContent = page.name;
        navbar.appendChild(link);
    });
});
