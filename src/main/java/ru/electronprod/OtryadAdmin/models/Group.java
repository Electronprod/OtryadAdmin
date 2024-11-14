package ru.electronprod.OtryadAdmin.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
public class Group implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(unique = true, nullable = false)
	private String name;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User marker;
	@ManyToMany(mappedBy = "groups", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Human> humans = new HashSet<>();

	public void addHuman(Human human) {
		humans.add(human);
		human.getGroups().add(this);
	}

	public void removeHuman(Human human) {
		humans.remove(human);
		human.getGroups().remove(this);
	}
}
