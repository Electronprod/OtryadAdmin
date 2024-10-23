function showWhoMarked() {
	Swal.fire({
		title: 'Введите дату',
		input: 'date',
		inputLabel: 'Выберите дату',
		inputValue: new Date().toISOString().split('T')[0],
		showCancelButton: true,
		inputValidator: (value) => {
			if (!value) {
				return 'Вы должны ввести дату!'
			}
		}
	}).then((result) => {
		if (result.isConfirmed) {
			const date = result.value;
			const url = `/api/observer/marks?date=${date}`;

			fetch(url)
				.then(response => response.json())
				.then(data => {
					let content = `
                            <table>
                                <thead>
                                    <tr>
                                        <th>Роль</th>
                                        <th>Логин</th>
                                        <th>События</th>
                                    </tr>
                                </thead>
                                <tbody>
                        `;

					data.forEach(item => {
						content += `
                                <tr>
                                    <td>${item.role}</td>
                                    <td>${item.login}</td>
                                    <td>${item.events.join(", ")}</td>
                                </tr>
                            `;
					});

					content += `
                                </tbody>
                            </table>
                        `;

					Swal.fire({
						title: 'Отметки за ' + date,
						html: content,
						width: 800,
						padding: '1em',
						background: '#fff',
						showCloseButton: true,
						icon: 'info'
					});
				})
				.catch(error => {
					console.log(error);
					Swal.fire({
						title: 'Ошибка',
						text: 'Не удалось получить данные',
						icon: 'error'
					});
				});
		}
	});
}