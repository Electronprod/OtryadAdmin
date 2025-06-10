function levenshtein(a, b) {
	const distance = Array(a.length + 1).fill(null).map(() =>
		Array(b.length + 1).fill(null)
	);
	for (let i = 0; i <= a.length; i += 1) {
		distance[i][0] = i;
	}
	for (let j = 0; j <= b.length; j += 1) {
		distance[0][j] = j;
	}
	for (let i = 1; i <= a.length; i += 1) {
		for (let j = 1; j <= b.length; j += 1) {
			const indicator = a[i - 1] === b[j - 1] ? 0 : 1;
			distance[i][j] = Math.min(
				distance[i][j - 1] + 1,
				distance[i - 1][j] + 1,
				distance[i - 1][j - 1] + indicator
			);
		}
	}
	return distance[a.length][b.length];
}
function getHumansFromTable(lastnamecol, namecol, idcol, tableId) {
	const table = document.getElementById(tableId);
	const humans = [];

	if (table) {
		const rows = table.querySelectorAll("tr");
		for (let i = 1; i < rows.length; i++) {
			const cells = rows[i].querySelectorAll("td");
			const lastname = cells[lastnamecol]?.textContent.trim();
			const name = cells[namecol]?.textContent.trim();
			const id = cells[idcol]?.textContent.trim();
			if (lastname && name) {
				humans.push({ lastname, name, id });
			}
		}
	}

	return humans;
}
function findMostSimiliarHuman(lastnameAndName, humans) {
	let mostSimilar = null;
	let minLevenshteinDistance = Number.MAX_VALUE;

	for (const human of humans) {
		const fullName = `${human.lastname} ${human.name}`;
		const levenshteinDistance = levenshtein(
			lastnameAndName.toLowerCase(),
			fullName.toLowerCase()
		);

		if (levenshteinDistance < minLevenshteinDistance) {
			minLevenshteinDistance = levenshteinDistance;
			mostSimilar = human;
		}
	}
	return mostSimilar;
}