function getRandomColor() {
	const letters = '0123456789ABCDEF';
	let color = '#';
	for (let i = 0; i < 6; i++) {
		color += letters[Math.floor(Math.random() * 16)];
	}
	return color;
}
function highlightRows(tableId, columnIndex) {
	const table = document.getElementById(tableId);
	const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
	const colorMap = {};
	for (let i = 0; i < rows.length; i++) {
		const cellValue = rows[i].getElementsByTagName('td')[columnIndex].innerText;
		if (colorMap[cellValue] !== undefined) {
			rows[i].style.backgroundColor = colorMap[cellValue];
		} else {
			const color = getRandomColor();
			colorMap[cellValue] = color;
			rows[i].style.backgroundColor = color;
		}
	}
}
function resetHighlightRows(tableId) {
	const table = document.getElementById(tableId);
	const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
	for (let i = 0; i < rows.length; i++) {
		rows[i].style.backgroundColor = '';
	}
}