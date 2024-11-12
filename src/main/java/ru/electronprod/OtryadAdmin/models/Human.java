package ru.electronprod.OtryadAdmin.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "people")
@NoArgsConstructor
@Getter
@Setter
public class Human implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@ManyToOne
	@JoinColumn(name = "squad_id")
	private Squad squad;

	@OneToMany(mappedBy = "human", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<SquadStats> stats;

	@ManyToMany(mappedBy = "humans", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Group> groups = new HashSet<>();

	@Column
	private String name;
	@Column
	private String lastname;
	@Column
	private String surname;
	@Column
	private String birthday;
	@Column
	private String school;
	@Column
	private String classnum;
	@Column
	private String phone;

	public void addDepartment(Group group) {
		groups.add(group);
		group.getHumans().add(this);
	}

	public void removeDepartment(Group group) {
		groups.remove(group);
		group.getHumans().remove(this);
	}

	public String showGroups() {
		String result = "";
		for (Group group : groups)
			result = result + ", " + group.getName();
		return result;
	}
}
