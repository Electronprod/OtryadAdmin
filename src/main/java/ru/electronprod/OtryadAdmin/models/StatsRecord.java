package ru.electronprod.OtryadAdmin.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stats")
@Getter
@Setter
@NoArgsConstructor
public class StatsRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne()
	@JoinColumn(name = "human_id", referencedColumnName = "id")
	private Human human;
	// Дата
	@Column
	private String date;
	// Тип
	@Column
	private String type;
	// Автор
	@Column
	private String author;
	// Роль пользователя
	@Column
	private String user_role;
	// Причина отсутствия
	@Column
	private String reason;
	// ID ивента
	@Column
	private int event_id;
	// Положительное - человек пришел, иначе - нет
	@Column
	private boolean isPresent;
	@Column(name="recordGroup")
	private String group;

	public StatsRecord(Human human) {
		this.human = human;
	}

	public String getType() {
		return this.type;
	}
}
