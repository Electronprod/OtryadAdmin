package ru.electronprod.OtryadAdmin.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "datetable")
@NoArgsConstructor
public class DateEvent {
	@Getter
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Getter
	@Column(name = "date")
	private String date;
	@Getter
	@Column(name = "content")
	private String content;
	@Getter
	@Column(name = "name")
	private String name;

	public DateEvent(String date, String content, String name) {
		this.content = content;
		this.date = date;
		this.name = name;
	}

	public DateEvent(String date, String content, String name, int id) {
		this.content = content;
		this.date = date;
		this.name = name;
	}
}
