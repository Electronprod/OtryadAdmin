function init_custom_option(selectorId, text) {
	var select = document.getElementById(selectorId);
	select.setAttribute("onchange", "custom_option_run('" + selectorId + "')");
	var customOption = document.createElement("option");
	customOption.value = "custom-option";
	customOption.textContent = text;
	select.appendChild(customOption);
}
function custom_option_run(selectorId) {
	var select = document.getElementById(selectorId);
	var selectedValue = select.value;
	if (selectedValue === "custom-option") {
		var eventName = prompt("Введите значение:");
		if (eventName) {
			var newOption = document.createElement("option");
			newOption.value = eventName;
			newOption.textContent = eventName;
			select.appendChild(newOption);
			select.value = eventName;
		}
	}
}