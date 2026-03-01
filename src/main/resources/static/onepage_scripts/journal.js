document.addEventListener('DOMContentLoaded', () => {
	applyLogStyles();
	const table = document.getElementById('userTable');
	const inputs = document.querySelectorAll('.filter-input');
	const rows = table.querySelectorAll('tbody tr');
	inputs.forEach(input => {
		input.addEventListener('input', () => {
			const filters = Array.from(inputs).map(i => ({
				index: i.getAttribute('data-col'),
				value: i.value.toLowerCase()
			})).filter(f => f.value !== "");
			rows.forEach(row => {
				const cells = row.querySelectorAll('td');
				const isVisible = filters.every(f => {
					const cellContent = cells[f.index].textContent.toLowerCase();
					return cellContent.includes(f.value);
				});
				row.style.display = isVisible ? '' : 'none';
			});
		});
	});
});
function applyLogStyles() {
    const styleMap = {
        'СИСТЕМА': 'log-row-system',
        'АВТОРИЗАЦИЯ': 'log-row-auth',
        'ВЫСТАВЛЕНИЕ ОТМЕТОК': 'log-row-mark',
        'ОШИБКА': 'log-row-mark-exception',
        'ТЕЛЕГРАМ': 'log-row-telegram',
        'АДМИНИСТРИРОВАНИЕ': 'log-row-admin',
        'СОЗДАНИЕ': 'log-row-create',
        'РЕДАКТИРОВАНИЕ': 'log-row-edit',
        'УДАЛЕНИЕ': 'log-row-remove',
        'ПРИЕМНАЯ': 'log-row-collector'
    };

    const rows = document.querySelectorAll('#userTable tbody tr');

    rows.forEach(row => {
        if (row.cells.length < 2) return;

        const typeCell = row.cells[1];
        const typeText = typeCell.textContent.trim().toUpperCase();

        row.className = '';

        for (const [key, className] of Object.entries(styleMap)) {
            if (typeText.includes(key)) {
                row.classList.add(className);
                
                if (!typeCell.querySelector('.log-badge')) {
                    const originalText = typeCell.textContent.trim();
                    typeCell.innerHTML = `<span class="log-badge">${originalText}</span>`;
                }
                break;
            }
        }
    });
}
document.addEventListener('DOMContentLoaded', function() {
    const messageCells = document.querySelectorAll('.message-cell');

    messageCells.forEach(cell => {
        cell.addEventListener('click', function() {
            const rawData = this.getAttribute('data-full-text');
            let displayHtml = '';
            try {
                const jsonObject = JSON.parse(rawData);
                const formattedJson = JSON.stringify(jsonObject, null, 4);
                displayHtml = `<pre style="text-align: left; background: #2d2d2d; color: #ccc; padding: 15px; border-radius: 8px; overflow-x: auto; font-size: 13px;"><code>${formattedJson}</code></pre>`;
            } catch (e) {
                displayHtml = `<div style="text-align: left; white-space: pre-wrap; word-break: break-all; padding: 10px; background: #f9fafb; border-radius: 8px; border: 1px solid #eee;">${rawData}</div>`;
            }
            Swal.fire({
                title: 'Детали записи',
                html: displayHtml,
                width: '80%',
                maxWidth: '800px',
                confirmButtonText: 'Закрыть',
                showCloseButton: true,
                customClass: {
                    container: 'my-swal-container'
                }
            });
        });
    });
});