package ru.electronprod.OtryadAdmin.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.utils.Answer;
import ru.electronprod.OtryadAdmin.utils.FileOptions;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper auth;
	// TODO
	@Autowired
	BuildProperties appInfo;

	/*
	 * Main page
	 */
	@GetMapping("")
	public String dash(Model model) {
		model.addAttribute("show_log", (new File("log.txt")).exists());
		return "admin/dashboard";
	}

	/*
	 * User manager
	 */
	@GetMapping("/usermgr")
	public String usermgr(Model model) {
		model.addAttribute("users", dbservice.getUserRepository().findAll());
		model.addAttribute("roles", SettingsRepository.getRoles());
		return "admin/usermgr";
	}

	@PostMapping("/usermgr/add")
	public ResponseEntity<String> userManager_addAction(@RequestParam String login, @RequestParam String password,
			@RequestParam String role, @RequestParam String name) {
		if (dbservice.getUserRepository().findByLogin(login).isPresent())
			return ResponseEntity.status(409).body(Answer.fail("Login is busy"));
		User user = new User();
		user.setLogin(login);
		user.setPassword(password);
		user.setRole(role);
		user.setName(name);
		if (auth.register(user))
			return ResponseEntity.accepted().body(Answer.success());
		return ResponseEntity.internalServerError().body(Answer.fail("Server error: can't save user to DB."));
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/usermgr/edit")
	public ResponseEntity<String> userManager_edit(@RequestParam int id) {
		Optional<User> user1 = dbservice.getUserRepository().findById(id);
		if (user1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("User not found"));
		User user = user1.get();
		JSONObject data = new JSONObject();
		data.put("login", user.getLogin());
		data.put("role", user.getRole());
		data.put("name", user.getName());
		return ResponseEntity.ok(data.toJSONString());
	}

	@PostMapping("/usermgr/edit")
	public ResponseEntity<String> userManager_editAction(@RequestParam String login, @RequestParam String password,
			@RequestParam String role, @RequestParam String name, int id) {
		Optional<User> user1 = dbservice.getUserRepository().findById(id);
		if (user1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("User not found"));
		User user = user1.get();
		user.setLogin(login);
		user.setName(name);
		if ((user.getSquad() != null || !user.getGroups().isEmpty()) && !user.getRole().equals(role))
			return ResponseEntity.badRequest()
					.body(Answer.fail("You can't change roles if you have a squad or group."));
		user.setRole(role);
		boolean result = false;
		if (password.equals("not_changed")) {
			result = dbservice.getUserRepository().save(user) != null;
		} else {
			user.setPassword(password);
			result = auth.register(user);
		}
		if (result)
			return ResponseEntity.accepted().body(Answer.success());
		return ResponseEntity.internalServerError().body(Answer.fail("Server error: can't save user to DB."));
	}

	@PostMapping("/usermgr/delete")
	public ResponseEntity<String> userManager_delete(@RequestParam() int id) {
		Optional<User> user = dbservice.getUserRepository().findById(id);
		if (user.isEmpty())
			return ResponseEntity.badRequest().body(Answer.fail("User not found"));
		if (!user.get().getGroups().isEmpty())
			return ResponseEntity.unprocessableEntity().body(Answer.fail("User has dependant group"));
		if (user.get().getSquad() != null)
			return ResponseEntity.unprocessableEntity().body(Answer.fail("User has dependant squad"));
		dbservice.getUserRepository().deleteById(id);
		return ResponseEntity.accepted().body(Answer.success());
	}

	/*
	 * Squad manager
	 */
	@GetMapping("/squadmgr")
	public String squadManager(Model model) {
		model.addAttribute("squads", dbservice.getSquadRepository().findAll());
		model.addAttribute("users", dbservice.getUserRepository().findAllByRole("ROLE_SQUADCOMMANDER").stream()
				.filter(user -> user.getSquad() == null).toList());
		return "admin/squadmgr";
	}

	@PostMapping("/squadmgr/add")
	public ResponseEntity<String> squadManager_addAction(@RequestParam String user, @RequestParam String name) {
		Optional<User> commander = dbservice.getUserRepository().findById(Integer.parseInt(user));
		if (commander.isPresent())
			ResponseEntity.status(409).body(Answer.fail("User already has squad"));
		Squad squad = new Squad();
		squad.setSquadName(name);
		squad.setCommander(commander.get());
		boolean result = dbservice.getSquadRepository().save(squad) != null;
		if (result)
			return ResponseEntity.ok(Answer.success());
		return ResponseEntity.internalServerError().body(Answer.fail("Server error: can't save squad to DB"));
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/squadmgr/edit")
	public ResponseEntity<String> squadManager_edit(@RequestParam int id) {
		Optional<Squad> squad1 = dbservice.getSquadRepository().findById(id);
		if (squad1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Squad not found"));
		Squad squad = squad1.get();
		JSONObject data = new JSONObject();
		data.put("name", squad.getSquadName());
		data.put("commander", squad.getCommander().getId());
		return ResponseEntity.ok(data.toJSONString());
	}

	@PostMapping("/squadmgr/edit")
	public ResponseEntity<String> squadManager_editAction(@RequestParam int id, @RequestParam String name,
			@RequestParam int user) {
		Optional<Squad> squad1 = dbservice.getSquadRepository().findById(id);
		Optional<User> commander = dbservice.getUserRepository().findById(user);
		if (squad1.isEmpty() || commander.isEmpty())
			ResponseEntity.status(404).body(Answer.fail("Squad or User not found"));
		Squad squad = squad1.get();
		squad.setSquadName(name);
		squad.setCommander(commander.get());
		boolean result = dbservice.getSquadRepository().save(squad) != null;
		if (result)
			return ResponseEntity.ok(Answer.success());
		return ResponseEntity.internalServerError().body(Answer.fail("Server error: can't save squad to DB"));
	}

	@PostMapping("/squadmgr/delete")
	public ResponseEntity<String> squadManager_delete(@RequestParam() int id) {
		if (dbservice.getSquadRepository().findById(id).isEmpty()) {
			return ResponseEntity.status(404).body(Answer.fail("Squad not found"));
		}
		dbservice.getSquadRepository().deleteById(id);
		return ResponseEntity.accepted().body(Answer.success());
	}

	/*
	 * Human manager
	 */
	@GetMapping("/humanmgr")
	public String humanManager(Model model) {
		List<Human> humans = dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname"));
		model.addAttribute("humans", humans);
		model.addAttribute("squads", dbservice.getSquadRepository().findAll());
		return "admin/humanmgr";
	}

	// TODO replace with something better
	@Deprecated
	@PostMapping("/humanmgr/addlist")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
		List<Human> records = new ArrayList<Human>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					String[] data = line.split(";");
					Human result = new Human();
					Squad squad = dbservice.getSquadRepository().findById(Integer.parseInt(data[0])).orElseThrow();
					result.setSquad(squad);
					result.setLastname(data[1]);
					result.setName(data[2]);
					result.setSurname(data[3]);
					result.setBirthday(data[4]);
					result.setSchool(data[5]);
					result.setClassnum(data[6]);
					result.setPhone(data[7]);
					records.add(result);
				} catch (Exception e) {
					log.error("Error parsing line: " + line);
					return "redirect:/admin/humanmgr?error&" + e.getMessage() + "&line" + line;
				}
			}
			dbservice.getHumanRepository().saveAll(records);
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/admin/humanmgr?error&" + e.getMessage();
		}
		return "redirect:/admin/humanmgr?saved";
	}

	@PostMapping("/humanmgr/add")
	public ResponseEntity<String> humanmgr_addUser(@RequestParam String name, @RequestParam String lastname,
			@RequestParam String surname, @RequestParam String birthday, @RequestParam String school,
			@RequestParam String classnum, @RequestParam String phone, @RequestParam int squad) {
		Optional<Squad> squadO = dbservice.getSquadRepository().findById(squad);
		if (squadO.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Squad not found"));
		Human human = new Human();
		human.setName(name);
		human.setLastname(lastname);
		human.setSurname(surname);
		human.setBirthday(birthday);
		human.setSchool(school);
		human.setClassnum(classnum);
		human.setPhone(phone);
		human.setSquad(squadO.get());
		boolean result = dbservice.getHumanRepository().save(human) != null;
		if (result)
			return ResponseEntity.ok(Answer.success());
		return ResponseEntity.internalServerError().body(Answer.fail("Server error: can't save human to DB"));
	}

	@GetMapping("/humanmgr/fullinfo")
	public String humanmgr_fullInfo(@RequestParam(required = false) String id, Model model) {
		try {
			if (id != null) {
				List<Human> humans = new ArrayList<Human>();
				humans.add(dbservice.getHumanRepository().findById(Integer.parseInt(id)).orElseThrow());
				model.addAttribute("humans", humans);
				return "public/humans_rawtable";
			} else {
				return "forward:/observer/data";
			}
		} catch (Exception e) {
			return "redirect:/admin/humanmgr";
		}
	}

	@PostMapping("/humanmgr/delete")
	public ResponseEntity<String> humanManager_delete(@RequestParam() int id) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Human not found"));
		dbservice.getHumanRepository().deleteById(id);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/humanmgr/edit")
	public ResponseEntity<String> humanManager_edit(@RequestParam int id) {
		Optional<Human> human1 = dbservice.getHumanRepository().findById(id);
		if (human1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Human not found"));
		Human human = human1.get();
		JSONObject data = new JSONObject();
		data.put("name", human.getName());
		data.put("lastname", human.getLastname());
		data.put("surname", human.getSurname());
		data.put("birthday", human.getBirthday());
		data.put("school", human.getSchool());
		data.put("classnum", human.getClassnum());
		data.put("phone", human.getPhone());
		data.put("squad", human.getSquad().getId());
		return ResponseEntity.ok(data.toJSONString());
	}

	@PostMapping("/humanmgr/edit")
	public ResponseEntity<String> humanmgr_editUser(@RequestParam int id, @RequestParam String name,
			@RequestParam String lastname, @RequestParam String surname, @RequestParam String birthday,
			@RequestParam String school, @RequestParam String classnum, @RequestParam String phone,
			@RequestParam int squad) {
		Optional<Human> h = dbservice.getHumanRepository().findById(id);
		if (h.isEmpty()) {
			return ResponseEntity.status(404).body(Answer.fail("Human not found"));
		}
		Optional<Squad> squadO = dbservice.getSquadRepository().findById(squad);
		if (squadO.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Squad not found"));
		Human human = h.get();
		human.setName(name);
		human.setLastname(lastname);
		human.setSurname(surname);
		human.setBirthday(birthday);
		human.setSchool(school);
		human.setClassnum(classnum);
		human.setPhone(phone);
		human.setSquad(squadO.get());
		boolean result = dbservice.getHumanRepository().save(human) != null;
		if (result)
			return ResponseEntity.ok(Answer.success());
		return ResponseEntity.internalServerError().body(Answer.fail("Server error: can't save human to DB"));
	}

	/*
	 * Group manager
	 */
	@GetMapping("/groupmgr")
	public String groupManager(Model model) {
		List<User> markers = dbservice.getUserRepository().findAllByRole("ROLE_COMMANDER");
		model.addAttribute("markers", markers);
		model.addAttribute("groups", dbservice.getGroupRepository().findAll());
		return "admin/groupmgr";
	}

	@PostMapping("/groupmgr/add")
	public String groupManager_addAction(@ModelAttribute Group group) {
		if (dbservice.getGroupRepository().save(group) != null)
			return "redirect:/admin/groupmgr?saved";
		return "redirect:/admin/groupmgr?error_unknown";
	}

	@PostMapping("/groupmgr/delete")
	@Transactional
	public ResponseEntity<String> groupManager_delete(@RequestParam() int id) throws InterruptedException {
		Optional<Group> group1 = dbservice.getGroupRepository().findById(id);
		if (group1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Group not found"));
		if (!group1.get().getHumans().isEmpty())
			return ResponseEntity.unprocessableEntity()
					.body(Answer.fail("Cannot delete group with people! Delete people from group and try again."));
		dbservice.getGroupRepository().deleteById(id);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@GetMapping("/groupmgr/manage")
	public String groupManager(Model model, @RequestParam int id) {
		Optional<Group> group1 = dbservice.getGroupRepository().findById(id);
		if (group1.isEmpty())
			return "redirect:/admin/groupmgr?error_notfound";
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		model.addAttribute("group", group1.get());
		return "admin/groupmgr_manage";
	}

	@PostMapping("/groupmgr/manage/add")
	public ResponseEntity<String> groupManager_add(@RequestParam() int id, @RequestParam() int humanid) {
		Optional<Human> human = dbservice.getHumanRepository().findById(humanid);
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (human.isEmpty() || group.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Group or Human not found"));
		Group gr = group.get();
		gr.addHuman(human.get());
		dbservice.getGroupRepository().save(gr);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@PostMapping("/groupmgr/manage/delete")
	public ResponseEntity<String> groupManager_delete(@RequestParam() int id, @RequestParam() int humanid) {
		Optional<Human> human1 = dbservice.getHumanRepository().findById(humanid);
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (human1.isEmpty() || group.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Group or Human not found"));
		Group gr = group.get();
		gr.removeHuman(human1.get());
		dbservice.getGroupRepository().save(gr);
		return ResponseEntity.accepted().body(Answer.success());
	}

	/*
	 * Stats manager
	 */
	@GetMapping("/statsmgr")
	public String statsManager(Model model) {
		model.addAttribute("event_types_map", SettingsRepository.getEvent_types());
		model.addAttribute("reasons_for_absences_map", SettingsRepository.getReasons_for_absences());
		return "admin/statsmgr";
	}

	@GetMapping("/statsmgr/table")
	public String statsManager_table(Model model) {
		return "forward:/observer/stats/table";
	}

	@GetMapping("/statsmgr/delete_event")
	public String statsManager_delete_byEventID(@RequestParam int id) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByEventId(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		dbservice.getStatsRepository().deleteAll(stats);
		return "redirect:/admin/statsmgr?deleted";
	}

	@GetMapping("/statsmgr/delete")
	public String statsManager_delete_byID(@RequestParam int id) {
		Optional<StatsRecord> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		dbservice.getStatsRepository().delete(stats.get());
		return "redirect:/admin/statsmgr?deleted";
	}

	@PostMapping("/statsmgr/edit_type")
	public String statsManager_edit_type(@RequestParam int eventid, @RequestParam String statsType) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByEventId(eventid);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.stream().forEach(stat -> stat.setType(statsType));
		dbservice.getStatsRepository().saveAll(stats);
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_type_single")
	public String statsManager_edit_type_single(@RequestParam int id, @RequestParam String statsType) {
		Optional<StatsRecord> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.get().setType(statsType);
		dbservice.getStatsRepository().save(stats.get());
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_date")
	public String statsManager_edit_date(@RequestParam int eventid, @RequestParam String date) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByEventId(eventid);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.stream().forEach(stat -> stat.setDate(date.replaceAll("-", ".")));
		dbservice.getStatsRepository().saveAll(stats);
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_date_single")
	public String statsManager_edit_date_single(@RequestParam int id, @RequestParam String date) {
		Optional<StatsRecord> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.get().setDate(date.replaceAll("-", "."));
		dbservice.getStatsRepository().save(stats.get());
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_reason")
	public String statsManager_edit_reason(@RequestParam int eventid, @RequestParam String reason) {
		List<StatsRecord> statsList = dbservice.getStatsRepository().findByEventId(eventid);
		statsList.removeIf(stats -> stats.isPresent());
		if (statsList.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		statsList.stream().forEach(stat -> stat.setReason(reason));
		dbservice.getStatsRepository().saveAll(statsList);
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_reason_single")
	public String statsManager_edit_reason_single(@RequestParam int id, @RequestParam String reason) {
		Optional<StatsRecord> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.get().setReason(reason);
		dbservice.getStatsRepository().save(stats.get());
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_type_all")
	public String statsManager_edit_type_all(@RequestParam String from, @RequestParam String to) {
		List<StatsRecord> statsList = dbservice.getStatsRepository().findByType(from);
		if (statsList.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		statsList.stream().forEach(stat -> stat.setType(to));
		dbservice.getStatsRepository().saveAll(statsList);
		return "redirect:/admin/statsmgr?edited";
	}

	@GetMapping("/config")
	public String config(Model model) {
		model.addAttribute("raw_config", FileOptions.getFileLine(SettingsRepository.getConfig()));
		model.addAttribute("eventtypes", SettingsRepository.getEvent_types());
		model.addAttribute("reasons", SettingsRepository.getReasons_for_absences());
		model.addAttribute("replacements", SettingsRepository.getReplacements());
		return "admin/config/config";
	}

	@PostMapping("/config/addevent")
	public String config_addevent(@RequestParam String name, @RequestParam String event,
			@RequestParam String canSetReason) {
		try {
			SettingsRepository.addData("event_types",
					SettingsRepository.generateEvent(event, name, Boolean.parseBoolean(canSetReason)));
			return "redirect:/admin/config?saved";
		} catch (ParseException e) {
			log.error("Error adding event. ", e);
			return "redirect:/admin/config?error_unknown";
		}
	}

	@PostMapping("/config/delevent")
	public String config_delevent(@RequestParam String event) {
		try {
			SettingsRepository.removeEvent(event);
			return "redirect:/admin/config?deleted";
		} catch (Exception e) {
			log.error("Error removing event. ", e);
			return "redirect:/admin/config?error_unknown";
		}
	}

	@PostMapping("/config/addreason")
	public String config_addreason(@RequestParam String name, @RequestParam String reason) {
		try {
			SettingsRepository.addData("reasons_for_absences", SettingsRepository.generateReason(reason, name));
			return "redirect:/admin/config?saved";
		} catch (ParseException e) {
			log.error("Error adding reason. ", e);
			return "redirect:/admin/config?error_unknown";
		}
	}

	@PostMapping("/config/delreason")
	public String config_delreason(@RequestParam String reason) {
		try {
			SettingsRepository.removeReason(reason);
			return "redirect:/admin/config?deleted";
		} catch (Exception e) {
			log.error("Error removing reason. ", e);
			return "redirect:/admin/config?error_unknown";
		}
	}

	@PostMapping("/config/addreplacement")
	public String config_addreplacement(@RequestParam String from, @RequestParam String to) {
		try {
			SettingsRepository.addData("replacements", SettingsRepository.generateReplacement(from, to));
			return "redirect:/admin/config?saved";
		} catch (ParseException e) {
			log.error("Error adding replacement. ", e);
			return "redirect:/admin/config?error_unknown";
		}
	}

	@PostMapping("/config/delreplacement")
	public String config_delreplacement(@RequestParam String from) {
		try {
			SettingsRepository.removeReplacement(from);
			return "redirect:/admin/config?deleted";
		} catch (Exception e) {
			log.error("Error removing replacement. ", e);
			return "redirect:/admin/config?error_unknown";
		}
	}

	@GetMapping("/log")
	public String log(Model model) {
		model.addAttribute("log", FileOptions.getFileLineWithSeparator(FileOptions.getFileLines("log.txt"), "\n"));
		return "admin/log";
	}
}
