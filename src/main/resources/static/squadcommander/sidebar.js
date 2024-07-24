document.addEventListener('DOMContentLoaded', function() {
	const sidebarMenuItems = [
		{ name: 'Обзор', url: '/squadcommander' },
		{ name: 'Список звена', url: '/squadcommander/humans' },
		{ name: 'Отметить', url: '/squadcommander/mark' },
		{ name: 'Статистика', url: '/squadcommander/stats' }
	];
	const sidebarMenu = document.querySelector('#sidebar ul');
	sidebarMenuItems.forEach(item => {
		const li = document.createElement('li');
		const link = document.createElement('a');
		link.href = item.url;
		link.textContent = item.name;
		li.appendChild(link);
		sidebarMenu.appendChild(li);
	});
});