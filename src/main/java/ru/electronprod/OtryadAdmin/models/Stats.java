package ru.electronprod.OtryadAdmin.models;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stats")
@Getter
@Setter
@NoArgsConstructor
public class Stats implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@OneToOne(mappedBy = "stats")
	@JoinColumn(name = "stats_id", referencedColumnName = "id")
	private Human human;
	@Column
	private int general_fee_count;
	@Column
	private String general_fee_dates;

	public Stats(Human human) {
		this.human = human;
	}
}
