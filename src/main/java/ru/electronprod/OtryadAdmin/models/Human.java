package ru.electronprod.OtryadAdmin.models;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "people")
@Getter
@Setter
@NoArgsConstructor
public class Human implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@ManyToOne
	@JoinColumn(name = "squad_id")
	private Squad squad;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Stats stats;

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
	private int classnum;
	@Column
	private String classchar;
	@Column
	private String address;
	@Column
	private String phone;
	@Column
	private int year_of_admission;
	@Column
	private boolean dedicated;
	@Column
	private String mother;
	@Column
	private String father;
}
