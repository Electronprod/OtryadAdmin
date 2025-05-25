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
	@ManyToOne
	@JoinColumn(name = "human_id", referencedColumnName = "id")
	private Human human;
	@Column
	private String date;
	@Column
	private String type;
	@Column
	private String author;
	@Column
	private String user_role;
	@Column
	private String reason;
	@Column
	private int event_id;
	@Column
	private boolean isPresent;
	@Column(name = "recordGroup")
	private String group;

	public StatsRecord(Human human) {
		this.human = human;
	}

	public String getType() {
		return this.type;
	}
}
