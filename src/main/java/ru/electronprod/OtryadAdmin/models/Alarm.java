package ru.electronprod.OtryadAdmin.models;

import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alarms")
@Getter
@Setter
@NoArgsConstructor
public class Alarm {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "event", nullable = false)
	private String eventName;
	@Column(name = "description")
	private String description;
	@Column(name = "dates", nullable = false)
	private Set<String> dates;
	// User's to send this alarm to
	@Column(name = "receivers", nullable = false)
	private Set<Long> receivers;
	/*
	 * False-notification, True-news
	 */
	@Column
	private boolean type;
}