package ru.electronprod.OtryadAdmin.models;

import java.sql.Timestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "actions_log")
@Getter
@Setter
@NoArgsConstructor
public class ActionRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Timestamp time;
	@Column(nullable = false)
	private String login;
	@Column(nullable = false)
	private String role;
	@Column(length = Integer.MAX_VALUE)
	private String message;
	@Column(nullable = false)
	private ActionRecordType type;

	public ActionRecord(String login, String role, String message, ActionRecordType type) {
		super();
		this.login = login;
		this.role = role;
		this.message = message;
		this.type = type;
		this.time = new Timestamp(System.currentTimeMillis());
	}
}
