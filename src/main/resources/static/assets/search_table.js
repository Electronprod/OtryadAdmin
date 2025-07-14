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
		}
		`
	document.head.appendChild(style);
});
function search_human(id0, id1, tableID) {
	const input = document.getElementById('searchInput');
	const filter = input.value.toLowerCase();
	const table = document.getElementById(tableID);
	const tbody = table.getElementsByTagName('tbody')[0];
	const rows = tbody.getElementsByTagName('tr');
	for (let i = 0; i < rows.length; i++) {
		const lastname = rows[i].getElementsByTagName('td')[id0];
		const firstname = rows[i].getElementsByTagName('td')[id1];
		if (lastname || firstname) {
			const lastnameText = lastname.textContent.toLowerCase();
			const firstnameText = firstname.textContent.toLowerCase();
			if (lastnameText.indexOf(filter) > -1
				|| firstnameText.indexOf(filter) > -1) {
				rows[i].style.display = '';
			} else {
				rows[i].style.display = 'none';
			}
		}
	}
}
document.getElementById("searchInput").addEventListener("keypress",
	function() {
		this.scrollIntoView(true);
	});