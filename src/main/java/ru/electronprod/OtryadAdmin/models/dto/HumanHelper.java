package ru.electronprod.OtryadAdmin.models.dto;

import lombok.Data;
import lombok.NonNull;
import ru.electronprod.OtryadAdmin.models.Human;

@Data
@Deprecated(forRemoval = true)
public class HumanHelper {
	private int stats_id;
	private int squad_id;

	private int id;

	private String name;
	private String lastname;
	private String surname;
	private String birthday;
	private String school;
	private String classnum;
	private String phone;
	private String details;

	public static HumanHelper fillDefaultValues(@NonNull Human human) {
		HumanHelper result = new HumanHelper();
		result.setName(human.getName());
		result.setLastname(human.getLastname());
		result.setSurname(human.getSurname());
		result.setBirthday(human.getBirthday());
		result.setSchool(human.getSchool());
		result.setClassnum(human.getClassnum());
		result.setPhone(human.getPhone());
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
		result.setPhone(human.getPhone());
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
		result.setPhone(human.getPhone());
		result.setId(human.getId());
		return result;
	}
}
