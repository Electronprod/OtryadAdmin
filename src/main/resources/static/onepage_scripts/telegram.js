document.getElementById('sendremind').addEventListener('submit',
	function(event) {
		event.preventDefault();
		sendData(this);
	});
document.getElementById('sendmessage').addEventListener('submit',
	function(event) {
		event.preventDefault();
		sendData(this);
	});
document.addEventListener("DOMContentLoaded", () => {
	sortTable(1, 'rating');
	sortTable(1, 'rating1');
	console.log("Sorted rating.");
});
function receiver_select(val) {
	if (val == -1) {
		document.getElementById('send_remaind_table_sq').style = 'display:true';
		document.getElementById('sendremaind_btn').style = 'display:none';
	} else {
		document.getElementById('send_remaind_table_sq').style = 'display:none';
		document.getElementById('sendremaind_btn').style = 'display:true';
	}
}
function sendRemind(val, id) {
	if (document.getElementById('sendremind_eventname').value == "") {
		showError('Введите название события!');
		return;
	}
	setSmall(true);
	var s = document.getElementById('remind_userid_selector');
	let oldVal = s.options[s.selectedIndex].value;
	s.options[s.selectedIndex].value = id;
	console.log("Change: " + oldVal + ">>" + s.options[s.selectedIndex].value);
	sendData(document.getElementById('sendremind'));
	s.options[s.selectedIndex].value = oldVal;
	console.log("Back to: " + s.options[s.selectedIndex].value);
	val.style = "background-color: darkred";
}