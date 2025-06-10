async function fetchData(url) {
	try {
		const response = await fetch(url);
		if (!response.ok) {
			throw new Error(`HTTP error code: ${response.status}`);
		}
		return await response.json();
	} catch (error) {
		console.error('Exception while retrieving data:', error);
		return null;
	}
}
function replaceText(replacements) {
	document.querySelectorAll('table td').forEach(function(td) {
		let originalText = td.textContent;
		Object.keys(replacements).forEach(key => {
			const regex = new RegExp(key, 'g');
			const newText = originalText.replace(regex, replacements[key].replace(/\(.*?\)/g, '').trim());
			if (newText !== originalText) {
				td.textContent = newText;
			}
		});
	});
}
const url = window.location.origin + '/api/getrenamerdata';
let replacements = null;
async function main() {
	if (window.location.href.includes("stop_renamer")) {
		console.log('Renamer stopped due to URL argument.');
		return;
	}
	if (replacements == null)
		replacements = await fetchData(url);
	if (replacements) {
		replaceText(replacements);
	}
}
window.onload = main;
