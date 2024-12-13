package ru.electronprod.OtryadAdmin.models;

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
public class Human {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@ManyToOne
	@JoinColumn(name = "squad_id")
	private Squad squad;

	@OneToMany(mappedBy = "human", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<StatsRecord> stats;

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
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "human_group", joinColumns = @JoinColumn(name = "human_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
	private Set<Group> groups = new HashSet<>();

	public boolean hasGroup(Group group) {
		return groups.contains(group);
	}

	public String showGroups() {
		String result = "[";
		for (Group group : groups) {
			result = result + group.getName() + ",";
		}
		if (result.endsWith(","))
			result = result.replaceFirst("(?s)(.*)" + ",", "$1" + "");
		return result + "]";
	}
}
