!function() {
	var today = moment();
	function Calendar(selector) {
		this.el = document.querySelector(selector);
		this.current = moment().date(1);
		this.draw();
		var current = document.querySelector('.today');
		if (current) {
			var self = this;
			window.setTimeout(function() {
				self.openDay(current);
			}, 500);
		}
	}
	Calendar.prototype.draw = function() {
		this.drawHeader();
		this.drawMonth();
	}
	Calendar.prototype.drawHeader = function() {
		var self = this;
		if (!this.header) {
			this.header = createElement('div', 'header');
			this.header.className = 'header';
			this.title = createElement('h1');
			var right = createElement('div', 'right');
			right.addEventListener('click', function() { self.nextMonth(); });
			var left = createElement('div', 'left');
			left.addEventListener('click', function() { self.prevMonth(); });
			this.header.appendChild(this.title);
			this.header.appendChild(right);
			this.header.appendChild(left);
			this.el.appendChild(this.header);
		}
		this.title.innerHTML = this.current.format('MMMM YYYY');
	}
	Calendar.prototype.drawMonth = function() {
		var self = this;
		if (this.month) {
			this.oldMonth = this.month;
			this.oldMonth.className = 'month out ' + (self.next ? 'next' : 'prev');
			this.oldMonth.addEventListener('webkitAnimationEnd', function() {
				self.oldMonth.parentNode.removeChild(self.oldMonth);
				self.month = createElement('div', 'month');
				self.backFill();
				self.currentMonth();
				self.fowardFill();
				self.el.appendChild(self.month);
				window.setTimeout(function() {
					self.month.className = 'month in ' + (self.next ? 'next' : 'prev');
				}, 16);
			});
		} else {
			this.month = createElement('div', 'month');
			this.el.appendChild(this.month);
			this.backFill();
			this.currentMonth();
			this.fowardFill();
			this.month.className = 'month new';
		}
	}
	Calendar.prototype.backFill = function() {
		var clone = this.current.clone();
		var dayOfWeek = clone.day();
		if (!dayOfWeek) { return; }
		clone.subtract('days', dayOfWeek + 1);
		for (var i = dayOfWeek; i > 0; i--) {
			this.drawDay(clone.add('days', 1));
		}
	}
	Calendar.prototype.fowardFill = function() {
		var clone = this.current.clone().add('months', 1).subtract('days', 1);
		var dayOfWeek = clone.day();
		if (dayOfWeek === 6) { return; }
		for (var i = dayOfWeek; i < 6; i++) {
			this.drawDay(clone.add('days', 1));
		}
	}
	Calendar.prototype.currentMonth = function() {
		var clone = this.current.clone();
		while (clone.month() === this.current.month()) {
			this.drawDay(clone);
			clone.add('days', 1);
		}
	}
	Calendar.prototype.getWeek = function(day) {
		if (!this.week || day.day() === 0) {
			this.week = createElement('div', 'week');
			this.month.appendChild(this.week);
		}
	}
	Calendar.prototype.drawDay = function(day) {
		var self = this;
		this.getWeek(day);
		var outer = createElement('div', this.getDayClass(day));
		outer.addEventListener('click', function() {
			self.openDay(this);
		});
		var name = createElement('div', 'day-name', day.format('ddd'));
		var number = createElement('div', 'day-number', day.format('DD'));
		outer.appendChild(name);
		outer.appendChild(number);
		this.week.appendChild(outer);
	}
	Calendar.prototype.getDayClass = function(day) {
		classes = ['day'];
		if (day.month() !== this.current.month()) {
			classes.push('other');
		} else if (today.isSame(day, 'day')) {
			classes.push('today');
		}
		return classes.join(' ');
	}
	Calendar.prototype.openDay = function(el) {
		var details, arrow;
		var dayNumber = +el.querySelectorAll('.day-number')[0].innerText || +el.querySelectorAll('.day-number')[0].textContent;
		var day = this.current.clone().date(dayNumber);
		var currentOpened = document.querySelector('.details');
		if (currentOpened && currentOpened.parentNode === el.parentNode) {
			details = currentOpened;
			arrow = document.querySelector('.arrow');
		} else {
			if (currentOpened) {
				currentOpened.addEventListener('webkitAnimationEnd', function() {
					currentOpened.parentNode.removeChild(currentOpened);
				});
				currentOpened.addEventListener('oanimationend', function() {
					currentOpened.parentNode.removeChild(currentOpened);
				});
				currentOpened.addEventListener('msAnimationEnd', function() {
					currentOpened.parentNode.removeChild(currentOpened);
				});
				currentOpened.addEventListener('animationend', function() {
					currentOpened.parentNode.removeChild(currentOpened);
				});
				currentOpened.className = 'details out';
			}
			details = createElement('div', 'details in');
			var arrow = createElement('div', 'arrow');
			details.appendChild(arrow);
			el.parentNode.appendChild(details);
		}
		this.renderEvents(details, day);
		arrow.style.left = el.offsetLeft - el.parentNode.offsetLeft + 27 + 'px';
	}
	Calendar.prototype.renderEvents = function(ele, day) {
		var currentWrapper = ele.querySelector('.events');
		var wrapper = createElement('div', 'events in' + (currentWrapper ? ' new' : ''));
		var sendform = createElement("div", "sendform");
		wrapper.appendChild(sendform);
		var form = document.createElement('form');
		form.setAttribute('action', '/public/event-calendar/save');
		form.setAttribute('method', 'GET');
		var textarea = document.createElement('textarea');
		textarea.setAttribute('placeholder', 'Расскажите об этом дне');
		textarea.setAttribute('required', "true");
		textarea.setAttribute('name', "content");
		form.appendChild(textarea);
		var nameInput = document.createElement('input');
		nameInput.setAttribute('type', 'text');
		nameInput.setAttribute('placeholder', 'Ваше ФИО');
		nameInput.setAttribute('required', "true");
		nameInput.setAttribute('name', "name");
		nameInput.classList.add('sender');
		form.appendChild(nameInput);
		var dateData = document.createElement('input');
		dateData.setAttribute('type', 'hidden');
		dateData.classList.add('date_data');
		dateData.setAttribute('value', String(day.format('L')).replace(/\//g, '_'));
		dateData.setAttribute('name', "date");
		form.appendChild(dateData);
		var submitButton = document.createElement('input');
		submitButton.setAttribute('type', 'submit');
		submitButton.setAttribute('value', 'Отправить');
		submitButton.classList.add('confirm');
		form.appendChild(submitButton);
		sendform.appendChild(form);
		var about = createElement("div", "about");
		wrapper.appendChild(about);
		var btn = document.createElement('input');
		btn.setAttribute('type', 'button');
		btn.setAttribute('value', 'Что писали об этом дне?');
		btn.classList.add('confirm');
		btn.addEventListener("click", function() {
			console.log("Requesting data from db for day: " + String(day.format('L')).replace(/\//g, '_'));
			fetch("/api/event-calendar/daydata?day=" + String(day.format('L')).replace(/\//g, '_'))
				.then(response => response.json())
				.then(data => {
					console.log("Received: ", data);
					let listHtml = '<ul>';
					data.forEach(item => {
						listHtml += `<li><strong>${item.by}:</strong> ${item.content}</li>`;
					});
					listHtml += '</ul>';
					Swal.fire({
						icon: 'info',
						title: 'Список событий: ' + String(day.format('L')),
						html: listHtml
					});
				})
				.catch(error => {
					Swal.fire({
						title: "Something went wrong :(",
						text: "Error: " + String(error),
						icon: "error"
					});
				});
		});
		about.appendChild(btn);
		if (currentWrapper) {
			currentWrapper.className = 'events out';
			currentWrapper.addEventListener('webkitAnimationEnd', function() {
				currentWrapper.parentNode.removeChild(currentWrapper);
				ele.appendChild(wrapper);
			});
			currentWrapper.addEventListener('oanimationend', function() {
				currentWrapper.parentNode.removeChild(currentWrapper);
				ele.appendChild(wrapper);
			});
			currentWrapper.addEventListener('msAnimationEnd', function() {
				currentWrapper.parentNode.removeChild(currentWrapper);
				ele.appendChild(wrapper);
			});
			currentWrapper.addEventListener('animationend', function() {
				currentWrapper.parentNode.removeChild(currentWrapper);
				ele.appendChild(wrapper);
			});
		} else {
			ele.appendChild(wrapper);
		}
	}
	Calendar.prototype.nextMonth = function() {
		this.current.add('months', 1);
		this.next = true;
		this.draw();
	}
	Calendar.prototype.prevMonth = function() {
		this.current.subtract('months', 1);
		this.next = false;
		this.draw();
	}
	window.Calendar = Calendar;
	function createElement(tagName, className, innerText) {
		var ele = document.createElement(tagName);
		if (className) {
			ele.className = className;
		}
		if (innerText) {
			ele.innderText = ele.textContent = innerText;
		}
		return ele;
	}
}();
!function() {
	function addDate(ev) { }
	const url = window.location.href;
	if (window.location.href.includes("success")) {
		Swal.fire({
			title: "Готово!",
			text: "Ваш ответ был сохранен.",
			icon: "success"
		});
	}
	if (window.location.href.includes("fail")) {
		Swal.fire({
			title: "Произошла ошибка!",
			text: "Попробуйте еще раз.",
			icon: "error"
		});
	}
	var calendar = new Calendar('#calendar');
}();
async function report(event) {
	event.preventDefault();
	const response = await Swal.fire({
		title: 'Введите секретный ключ',
		input: 'text',
		showCancelButton: true,
		confirmButtonText: 'Подтвердить',
		cancelButtonText: 'Отмена'
	});
	if (response.isDismissed) { return; }
	console.log("Secret key: " + response.value);
	window.location.href = '/public/event-calendar/report?key=' + encodeURIComponent(response.value);
}