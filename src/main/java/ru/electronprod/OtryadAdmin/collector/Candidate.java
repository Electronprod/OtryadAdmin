package ru.electronprod.OtryadAdmin.collector;

import org.json.simple.JSONObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
class Candidate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "surname", nullable = false)
	private String surname;
	@Column(name = "firstname", nullable = false)
	private String firstname;
	@Column(name = "patronymic")
	private String patronymic;
	@Column(name = "dob")
	private String dob;
	@Column(name = "school")
	private String school;
	@Column(name = "grade")
	private String grade;
	@Column(name = "address")
	private String address;
	@Column(name = "parent", nullable = false)
	private String parent;
	@Column(name = "phone", nullable = false)
	private String phone;
	@Column(name = "notes", length = Integer.MAX_VALUE)
	private String notes;
	@Column(name = "photo", length = Integer.MAX_VALUE)
	private String photo;

	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject a = new JSONObject();
		a.put("surname", this.surname);
		a.put("firstname", this.firstname);
		a.put("patronymic", this.patronymic);
		a.put("dob", this.dob);
		a.put("school", this.school);
		a.put("grade", this.grade);
		a.put("address", this.address);
		a.put("parent", this.parent);
		a.put("phone", this.phone);
		a.put("notes", this.notes);
		a.put("photo", this.photo);
		return a;
	}
}
