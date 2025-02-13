package ru.electronprod.OtryadAdmin.models;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "telegram_users")
@Getter
@Setter
@NoArgsConstructor
public class Chat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long chatId;
	private String username;
	private Timestamp regtime;
	private String firstname;
	private String lastname;
	@org.hibernate.annotations.ColumnDefault("true")
	private boolean sendMarkedNotification;
	@org.hibernate.annotations.ColumnDefault("0")
	private int remaindsCounter;
	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User owner;

	public void setOwner(User owner) {
		this.owner = owner;
		if (owner != null) {
			owner.setTelegram(this);
		}
	}
}
