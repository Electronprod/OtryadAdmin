package ru.electronprod.OtryadAdmin.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "people")
@Data
public class Human {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
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
	@Column
	private String squadid;
}
