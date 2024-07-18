package ru.electronprod.OtryadAdmin.models;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "squads")
@Getter
@Setter
@NoArgsConstructor
public class Squad implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private User commander;
	@Column(name = "name", nullable = false)
	private String squadName;
	@Column(name = "commander_name")
	private String commanderName;
	@OneToMany(mappedBy = "squad", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Human> humans;

	public void addHuman(Human human) {
		humans.add(human);
		human.setSquad(this);
	}

	public void removeHuman(Human human) {
		humans.remove(human);
		human.setSquad(null);
	}
}