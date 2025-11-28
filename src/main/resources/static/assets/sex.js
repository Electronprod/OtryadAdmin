function getGenderByPatronymic(patronymic) {
	const p = patronymic.toLowerCase();
	const femaleEndings = ['овна', 'евна', 'ична', 'инична', 'а'];
	const maleEndings = ['ович', 'евич', 'ич', 'ч'];
	if (femaleEndings.some(end => p.endsWith(end))) {
		return 0;
	}
	if (maleEndings.some(end => p.endsWith(end))) {
		return 1;
	}
	return 2;
}
async function showSexColumn(tableID = 'userTable', createHeader = false, PatronymicIndex = 3) {
	const table = document.getElementById(tableID);
	if (createHeader) {
		const theadRow = table.querySelector('thead tr');
		const th = document.createElement('th');
		const btn = document.createElement('button');
		btn.onclick = () => sortTable(th.cellIndex + 1, tableID);
		btn.textContent = "↓";
		th.appendChild(btn)
		theadRow.appendChild(th);
	}
	table.querySelectorAll('tbody tr').forEach(row => {
		const td = document.createElement('td');
		const children = row.children;
		let val = "N/A";
		let sex = getGenderByPatronymic(children[PatronymicIndex].textContent.trim());
		switch (sex) {
			case 0:
				val = "Ж";
				break;
			case 1:
				val = "М";
				break;
			default:
				val = "N/A";
		}
		td.textContent = val;
		row.appendChild(td);
	});
}
function sortByGender(tableID = "markTable") {
	const tbody = document.getElementById(tableID).querySelector('tbody');
	const rows = Array.from(tbody.querySelectorAll('tr'));
	rows.sort((a, b) => {
		const patronymicA = a.querySelector('td:nth-child(4)').innerText.trim();
		const patronymicB = b.querySelector('td:nth-child(4)').innerText.trim();
		const gA = getGenderByPatronymic(patronymicA);
		const gB = getGenderByPatronymic(patronymicB);
		return gA - gB;
	});
	rows.forEach(row => tbody.appendChild(row));
}