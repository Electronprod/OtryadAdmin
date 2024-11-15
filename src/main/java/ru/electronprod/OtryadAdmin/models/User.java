package ru.electronprod.OtryadAdmin.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	@Column(name = "login", unique = true)
	private String login;
	@Column(name = "password")
	private String password;
	@Column(name = "role")
	private String role;
	@Column(name = "telegram")
	private String telegram;
	@Column(name = "vkID")
	private String vkID;
	@OneToOne(mappedBy = "commander", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private Squad squad;
	@OneToMany(mappedBy = "marker", orphanRemoval = false)
	private Set<Group> groups = new HashSet<Group>();
}
