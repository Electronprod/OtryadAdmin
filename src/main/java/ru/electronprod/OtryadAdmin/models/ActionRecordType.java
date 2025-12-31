package ru.electronprod.OtryadAdmin.models;

public enum ActionRecordType {
	SYSTEM("СИСТЕМА"), AUTH("Авторизация"), MARK("Выставление отметок"), MARK_EXCEPTION("Ошибка выставления отметок"),
	TELEGRAM("Телеграм"), ADMIN("Администрирование"), CREATE("Создание"), EDIT("Редактирование"), REMOVE("Удаление"),
	COLLECTOR("Приемная");

	public String name;

	public String getName() {
		return name;
	}

	ActionRecordType(String name) {
		this.name = name;
	}
}