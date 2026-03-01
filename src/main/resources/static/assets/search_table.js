document.addEventListener('DOMContentLoaded', function() {
	const style = document.createElement('style');
	style.textContent = `
        #searchInput {
            flex: 1 1 calc(50% - 20px);
            padding: 8px 10px;
            border: 1px solid #ccc;
            border-radius: 6px;
            transition: border-color 0.3s ease-in-out;
            font-size: 14px;
            margin-bottom: 10px;
            width: 100%;
        }
        #searchInput:focus {
            border-color: #3498db;
            outline: none;
        }
        /* Скрываем строку только если поиск пометил её как "не подходящую" */
        tr[data-search-hidden="true"] {
            display: none !important;
        }
    `;
	document.head.appendChild(style);
});
function search_human(id0, id1, tableID) {
	const input = document.getElementById('searchInput');
	const filter = input.value.toLowerCase().trim();
	const table = document.getElementById(tableID);
	const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
	for (let i = 0; i < rows.length; i++) {
		if (filter === "") {
			rows[i].removeAttribute('data-search-hidden');
			continue;
		}
		const lastname = rows[i].getElementsByTagName('td')[id0];
		const firstname = rows[i].getElementsByTagName('td')[id1];
		if (lastname || firstname) {
			const lastnameText = lastname ? lastname.textContent.toLowerCase() : "";
			const firstnameText = firstname ? firstname.textContent.toLowerCase() : "";
			const isMatch = lastnameText.includes(filter) || firstnameText.includes(filter);
			if (isMatch) {
				rows[i].setAttribute('data-search-hidden', 'false');
			} else {
				rows[i].setAttribute('data-search-hidden', 'true');
			}
		}
	}
}
async function showSearchInput(value) {
	const input = document.getElementById('searchInput');
	if (!input) return;

	if (value) {
		input.style.display = '';
	} else {
		input.style.display = 'none';
		input.value = '';
		search_human(0, 1, 'markTable');
	}
}
document.getElementById("searchInput").addEventListener("keypress", function() {
	this.scrollIntoView({ behavior: 'smooth', block: 'center' });
});