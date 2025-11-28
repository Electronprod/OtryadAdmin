let currentSortColumn = -1;
let currentSortDir = 'asc';

function sortTable(columnIndex, tableID = "userTable") {
	const table = document.getElementById(tableID);
	const tbody = table.querySelector('tbody');
	const rows = Array.from(tbody.rows);
	let dir = 'asc';
	if (currentSortColumn === columnIndex) {
		dir = currentSortDir === 'asc' ? 'desc' : 'asc';
	}
	currentSortColumn = columnIndex;
	currentSortDir = dir;
	rows.sort((a, b) => {
		const cellA = a.cells[columnIndex].innerText;
		const cellB = b.cells[columnIndex].innerText;
		if (dir === 'asc') {
			return cellA.localeCompare(cellB, 'ru', { numeric: true });
		} else {
			return cellB.localeCompare(cellA, 'ru', { numeric: true });
		}
	});
	rows.forEach(row => tbody.appendChild(row));
}