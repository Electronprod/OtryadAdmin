package ru.electronprod.OtryadAdmin.models;

import lombok.Data;
import lombok.NonNull;

@Data
public class HumanHelper {
	private int stats_id;
	private int squad_id;

	private int id;

	private String name;
	private String lastname;
	private String surname;
	private String birthday;
	private String school;
	private int classnum;
	private String classchar;
	private String address;
	private String phone;
	private int year_of_admission;
	private boolean dedicated;
	private String mother;
	private String father;

	public static HumanHelper fillDefaultValues(@NonNull Human human) {
		HumanHelper result = new HumanHelper();
		result.setName(human.getName());
		result.setLastname(human.getLastname());
		result.setSurname(human.getSurname());
		result.setBirthday(human.getBirthday());
		result.setSchool(human.getSchool());
		result.setClassnum(human.getClassnum());
		result.setClasschar(human.getClasschar());
		result.setAddress(human.getAddress());
		result.setPhone(human.getPhone());
		result.setYear_of_admission(human.getYear_of_admission());
		result.setDedicated(human.isDedicated());
		result.setMother(human.getMother());
		result.setFather(human.getFather());
		result.setId(human.getId());
		return result;
	}

	public static Human fillDefaultValues(@NonNull HumanHelper human) {
		Human result = new Human();
		result.setName(human.getName());
		result.setLastname(human.getLastname());
		result.setSurname(human.getSurname());
		result.setBirthday(human.getBirthday());
		result.setSchool(human.getSchool());
		result.setClassnum(human.getClassnum());
		result.setClasschar(human.getClasschar());
		result.setAddress(human.getAddress());
		result.setPhone(human.getPhone());
		result.setYear_of_admission(human.getYear_of_admission());
		result.setDedicated(human.isDedicated());
		result.setMother(human.getMother());
		result.setFather(human.getFather());
		result.setId(human.getId());
		return result;
	}

	public static Human fillDefaultValues(@NonNull HumanHelper human, @NonNull Human result) {
		result.setName(human.getName());
		result.setLastname(human.getLastname());
		result.setSurname(human.getSurname());
		result.setBirthday(human.getBirthday());
		result.setSchool(human.getSchool());
		result.setClassnum(human.getClassnum());
		result.setClasschar(human.getClasschar());
		result.setAddress(human.getAddress());
		result.setPhone(human.getPhone());
		result.setYear_of_admission(human.getYear_of_admission());
		result.setDedicated(human.isDedicated());
		result.setMother(human.getMother());
		result.setFather(human.getFather());
		result.setId(human.getId());
		return result;
	}
}
