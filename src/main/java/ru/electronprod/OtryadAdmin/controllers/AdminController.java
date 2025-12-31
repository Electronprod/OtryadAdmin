package ru.electronprod.OtryadAdmin.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.simple.JSONArray;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.ChatRepository;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.ActionRecordType;
import ru.electronprod.OtryadAdmin.models.Chat;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.MarkDTO;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.services.MarkService;
import ru.electronprod.OtryadAdmin.services.RecordService;
import ru.electronprod.OtryadAdmin.telegram.BotService;
import ru.electronprod.OtryadAdmin.utils.Answer;
import ru.electronprod.OtryadAdmin.utils.FileOptions;

/**
 * Spring MVC Controller object. Receives and processes HTTP requests in
 * "/admin" section of site.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private RecordService rec;
	@Autowired
	private AuthHelper auth;
	@Autowired
	private ChatRepository chatRep;
	@Autowired
	private MarkService statsHandler;
	@Autowired
	private BotService botServ;
	@Autowired
	private BuildProperties appInfo;

	/*
	 * Main page
	 */
	@GetMapping("")
	public String dash(Model model) {
		model.addAttribute("raw_config", FileOptions.getFileLine(SettingsRepository.getConfig()));
		model.addAttribute("eventtypes", SettingsRepository.getEvent_types());
		model.addAttribute("reasons", SettingsRepository.getReasons_for_absences());
		model.addAttribute("replacements", SettingsRepository.getReplacements());
		model.addAttribute("groups", dbservice.getGroupRepository().findAll());
		model.addAttribute("appInfo", appInfo);
		return "admin/dashboard";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "admin/mark";
	}

	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody MarkDTO dto) {
		User realUser = auth.getCurrentUser();
		try {
			List<Human> people = dbservice.getHumanRepository().findAll();
			User admin = new User();
			admin.setRole("ROLE_COMMANDER");
			admin.setId(realUser.getId());
			admin.setLogin(realUser.getLogin());
			admin.setTelegram(realUser.getTelegram());
			int event_id = statsHandler.mark_group(dto, admin, people, null);
			return ResponseEntity.accepted().body(Answer.marked(event_id));
		} catch (Exception e) {
			log.error("Mark error (admin.mark):", e);
			rec.recordAction(realUser, e.getMessage(), ActionRecordType.MARK_EXCEPTION);
			return ResponseEntity.internalServerError().body(Answer.fail(e.getMessage()));
		}
	}

	/*
	 * User manager
	 */
	@GetMapping("/usermgr")
	public String usermgr(Model model) {
		var users = dbservice.getUserRepository().findAll(Sort.by(Sort.Direction.ASC, "login"));
		model.addAttribute("users", users);
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
		auth.register(user);
		rec.recordAction(auth.getCurrentUser(), "User '%s' was created.".formatted(login), ActionRecordType.CREATE);
		return ResponseEntity.accepted().body(Answer.success());
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
		user.setName(name);
		if ((user.getSquad() != null || !user.getGroups().isEmpty()) && !user.getRole().equals(role))
			return ResponseEntity.badRequest()
					.body(Answer.fail("You can't change roles if the user have a squad or a group."));
		user.setRole(role);
		if (password.equals("not_changed")) {
			dbservice.getUserRepository().save(user);
		} else {
			user.setPassword(password);
			auth.register(user);
		}
		rec.recordAction(auth.getCurrentUser(), "User '%s' was edited.".formatted(login), ActionRecordType.EDIT);
		return ResponseEntity.accepted().body(Answer.success());
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
		rec.recordAction(auth.getCurrentUser(), "User '%s' was removed.".formatted(user.get().getLogin()),
				ActionRecordType.REMOVE);
		return ResponseEntity.accepted().body(Answer.success());
	}

	/*
	 * Squad manager
	 */
	@GetMapping("/squadmgr")
	public String squadManager(Model model) {
		model.addAttribute("squads", dbservice.getSquadRepository().findAll(Sort.by(Sort.Direction.ASC, "squadName")));
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
		dbservice.getSquadRepository().save(squad);
		rec.recordAction(auth.getCurrentUser(), "Squad '%s' was created.".formatted(name), ActionRecordType.CREATE);
		return ResponseEntity.ok(Answer.success());
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/squadmgr/edit")
	public ResponseEntity<String> squadManager_edit(@RequestParam int id) {
		Optional<Squad> squad1 = dbservice.getSquadRepository().findById(id);
		if (squad1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Squad not found"));
		JSONObject data = new JSONObject();
		data.put("name", squad1.get().getSquadName());
		return ResponseEntity.ok(data.toJSONString());
	}

	@PostMapping("/squadmgr/edit")
	public ResponseEntity<String> squadManager_editAction(@RequestParam int id, @RequestParam String name) {
		Optional<Squad> squad1 = dbservice.getSquadRepository().findById(id);
		if (squad1.isEmpty())
			ResponseEntity.status(404).body(Answer.fail("Squad not found"));
		Squad squad = squad1.get();
		squad1.get().setSquadName(name);
		dbservice.getSquadRepository().save(squad);
		rec.recordAction(auth.getCurrentUser(), "Squad(ID: %d) name was changed to %s.".formatted(squad.getId(), name),
				ActionRecordType.EDIT);
		return ResponseEntity.ok(Answer.success());
	}

	@PostMapping("/squadmgr/delete")
	public ResponseEntity<String> squadManager_delete(@RequestParam() int id) {
		var sq = dbservice.getSquadRepository().findById(id);
		if (sq.isEmpty()) {
			return ResponseEntity.status(404).body(Answer.fail("Squad not found"));
		}
		User user = sq.get().getCommander();
		user.setSquad(null);
		dbservice.getSquadRepository().deleteById(id);
		rec.recordAction(auth.getCurrentUser(), "Squad '%s' was removed.".formatted(sq.get().getSquadName()),
				ActionRecordType.REMOVE);
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
		dbservice.getHumanRepository().save(human);
		rec.recordAction(auth.getCurrentUser(), "Person '%s %s' was created.".formatted(name, lastname),
				ActionRecordType.CREATE);
		return ResponseEntity.ok(Answer.success());
	}

	@GetMapping("/humanmgr/fullinfo")
	public String humanmgr_fullInfo(@RequestParam(required = false) String id, Model model) {
		try {
			if (id != null) {
				model.addAttribute("humans",
						dbservice.getHumanRepository().findById(Integer.parseInt(id)).orElseThrow());
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
		rec.recordAction(auth.getCurrentUser(),
				"Person '%s %s' was removed.".formatted(human.get().getName(), human.get().getLastname()),
				ActionRecordType.REMOVE);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@GetMapping("/humanmgr/deleteall")
	public String humanManager_deleteAll(@RequestParam() long c) {
		if (dbservice.getHumanRepository().count() != c)
			return "redirect:/admin/humanmgr?deleteall_incorrect_number";
		dbservice.getHumanRepository().deleteAll();
		rec.recordAction(auth.getCurrentUser(), "All people were removed", ActionRecordType.REMOVE);
		return "redirect:/admin/humanmgr?deleted_all";
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
		dbservice.getHumanRepository().save(human);
		rec.recordAction(auth.getCurrentUser(),
				"Person '%s %s' was edited.".formatted(human.getName(), human.getLastname()), ActionRecordType.EDIT);
		return ResponseEntity.ok(Answer.success());
	}

	@GetMapping("/humanmgr/upload")
	public String humanmgr_upload(Model model) {
		return "admin/humanmgr_upload";
	}

	@PostMapping("/humanmgr/upload/csv")
	public String humanmgr_upload_csv(@RequestParam("file") MultipartFile file, Model model) {
		List<Human> records = new ArrayList<Human>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					String[] data = line.split(";");
					Human result = new Human();
					Squad squad = dbservice.getSquadRepository().findById(Integer.parseInt(data[0]))
							.orElseThrow(() -> new Exception("Squad not found"));
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
					return "redirect:/admin/humanmgr/upload?error=" + URLEncoder.encode(e.getMessage(), "UTF-8")
							+ "&line=" + URLEncoder.encode(line, "UTF-8");
				}
			}
			dbservice.getHumanRepository().saveAll(records);
			rec.recordAction(auth.getCurrentUser(), "Added %d persons from .CSV".formatted(records.size()),
					ActionRecordType.ADMIN);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				return "redirect:/admin/humanmgr/upload?error=" + URLEncoder.encode(e.getMessage(), "UTF-8")
						+ "&line=''";
			} catch (UnsupportedEncodingException e1) {
				return "redirect:/admin/humanmgr/upload?error=error_encoding_trace&line=''";
			}
		}
		return "redirect:/admin/humanmgr/upload?saved";
	}

	/*
	 * Group manager
	 */
	@GetMapping("/groupmgr")
	public String groupManager(Model model) {
		var markers = dbservice.getUserRepository().findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
				.filter(user -> user.getRole().equals("ROLE_COMMANDER")).toList();
		model.addAttribute("markers", markers);
		model.addAttribute("groups", dbservice.getGroupRepository().findAll());
		return "admin/groupmgr";
	}

	@PostMapping("/groupmgr/add")
	public String groupManager_addAction(@ModelAttribute Group group) {
		dbservice.getGroupRepository().save(group);
		rec.recordAction(auth.getCurrentUser(),
				"Created group '%s' with commander '%s'".formatted(group.getName(), group.getMarker().getName()),
				ActionRecordType.CREATE);
		return "redirect:/admin/groupmgr?saved";
	}

	@PostMapping("/groupmgr/change_editable")
	public ResponseEntity<String> groupManager_change_editable(@RequestParam int id) {
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (group.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Group not found"));
		Group gr = group.get();
		gr.setEditable(!gr.isEditable());
		dbservice.getGroupRepository().save(gr);
		rec.recordAction(auth.getCurrentUser(),
				"Set group '%s' editable to %b".formatted(gr.getName(), gr.isEditable()), ActionRecordType.EDIT);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@PostMapping("/groupmgr/change_reqmarks")
	public ResponseEntity<String> groupManager_change_reqmarks(@RequestParam() int id) {
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (group.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Group not found"));
		Group gr = group.get();
		gr.setRequireAbsentMark(!gr.isRequireAbsentMark());
		dbservice.getGroupRepository().save(gr);
		rec.recordAction(auth.getCurrentUser(),
				"Set required marks in group '%s' to %b".formatted(gr.getName(), gr.isRequireAbsentMark()),
				ActionRecordType.EDIT);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@PostMapping("/groupmgr/delete")
	public ResponseEntity<String> groupManager_delete(@RequestParam() int id) throws InterruptedException {
		Optional<Group> group1 = dbservice.getGroupRepository().findById(id);
		if (group1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Group not found"));
		if (!group1.get().getHumans().isEmpty())
			return ResponseEntity.unprocessableEntity()
					.body(Answer.fail("Cannot delete group with people! Delete people from group and try again."));
		if (SettingsRepository.containsGroup(group1.get().getName()))
			return ResponseEntity.unprocessableEntity().body(Answer.fail("The group is tied to some events"));
		dbservice.getGroupRepository().deleteById(id);
		rec.recordAction(auth.getCurrentUser(), "Removed group '%s'".formatted(group1.get().getName()),
				ActionRecordType.REMOVE);
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
		if (gr.getHumans().contains(human.get()))
			return ResponseEntity.status(404).body(Answer.fail("Human is already in the group!"));
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
		if (!gr.getHumans().contains(human1.get()))
			return ResponseEntity.status(404).body(Answer.fail("Human is not in the group!"));
		gr.removeHuman(human1.get());
		dbservice.getGroupRepository().save(gr);
		return ResponseEntity.accepted().body(Answer.success());
	}

	/**
	 * @param id     - groupID
	 * @param action - true/false - add/delete
	 */
	@PostMapping("/groupmgr/manage/all")
	public ResponseEntity<String> groupManager_all_action(@RequestParam() int id, @RequestParam() boolean action) {
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (group.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Group not found"));
		Group gr = group.get();
		if (action) {
			var people = dbservice.getHumanRepository().findAll();
			people.removeAll(gr.getHumans());
			people.forEach(human -> {
				gr.addHuman(human);
			});
		} else {
			var people = new ArrayList<Human>();
			people.addAll(gr.getHumans());
			people.forEach(human -> {
				gr.removeHuman(human);
			});
		}
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

	@SuppressWarnings("unchecked")
	@GetMapping("/statsmgr/data")
	public ResponseEntity<String> statsManager_getData(@RequestParam int number) {
		JSONArray arr = new JSONArray();
		dbservice.getStatsRepository().findLastRecords(number, 100).forEach(stats -> {
			JSONObject temp = new JSONObject();
			temp.put("date", stats.getDate());
			temp.put("lastname", stats.getHuman().getLastname());
			temp.put("name", stats.getHuman().getName());
			temp.put("type", stats.getType());
			temp.put("isPresent", stats.isPresent());
			temp.put("reason", stats.getReason());
			temp.put("author", stats.getAuthor());
			temp.put("group", stats.getGroup());
			temp.put("event_id", stats.getEvent_id());
			temp.put("id", stats.getId());
			arr.add(temp);
		});
		return ResponseEntity.ok(arr.toJSONString());
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/statsmgr/edit")
	public ResponseEntity<String> statsManager_editRecord(@RequestParam int id) {
		Optional<StatsRecord> stats1 = dbservice.getStatsRepository().findById(id);
		if (stats1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Record not found"));
		StatsRecord stats = stats1.get();
		JSONObject temp = new JSONObject();
		temp.put("date", stats.getDate());
		temp.put("type", stats.getType());
		temp.put("isPresent", stats.isPresent());
		temp.put("reason", stats.getReason());
		temp.put("author", stats.getAuthor());
		temp.put("event_id", stats.getEvent_id());
		temp.put("id", stats.getId());
		return ResponseEntity.ok(temp.toJSONString());
	}

	@PostMapping("/statsmgr/edit")
	public ResponseEntity<String> statsManager_editRecordAction(@RequestParam int id, @RequestParam String date,
			@RequestParam String type, @RequestParam boolean visit, @RequestParam String reason,
			@RequestParam String author) {
		Optional<StatsRecord> stats1 = dbservice.getStatsRepository().findById(id);
		if (stats1.isEmpty())
			ResponseEntity.status(404).body(Answer.fail("Record not found"));
		StatsRecord stats = stats1.get();
		stats.setDate(date);
		stats.setType(type);
		stats.setReason(reason);
		stats.setAuthor(author);
		stats.setPresent(visit);
		dbservice.getStatsRepository().save(stats);
		rec.recordAction(auth.getCurrentUser(), "Edited StatsRecord (ID: %d)".formatted(stats.getId()),
				ActionRecordType.EDIT);
		return ResponseEntity.ok(Answer.success());
	}

	@PostMapping("/statsmgr/delete_event")
	public ResponseEntity<String> statsManager_delete_byEventID(@RequestParam int id) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByEventId(id);
		if (stats.isEmpty()) {
			return ResponseEntity.status(404).body(Answer.fail("Event not found"));
		}
		dbservice.getStatsRepository().deleteAll(stats);
		rec.recordAction(auth.getCurrentUser(), "Removed StatsRecords (EventID: %d)".formatted(id),
				ActionRecordType.REMOVE);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@PostMapping("/statsmgr/delete")
	public ResponseEntity<String> statsManager_delete_byID(@RequestParam int id) {
		Optional<StatsRecord> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return ResponseEntity.status(404).body(Answer.fail("Record not found"));
		}
		dbservice.getStatsRepository().delete(stats.get());
		rec.recordAction(auth.getCurrentUser(), "Removed StatsRecord (ID: %d)".formatted(id), ActionRecordType.REMOVE);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@PostMapping("/statsmgr/edit_type")
	public ResponseEntity<String> statsManager_edit_type(@RequestParam int eventid, @RequestParam String value) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByEventId(eventid);
		if (stats.isEmpty()) {
			return ResponseEntity.status(404).body(Answer.fail("Event not found"));
		}
		stats.stream().forEach(stat -> stat.setType(value));
		dbservice.getStatsRepository().saveAll(stats);
		rec.recordAction(auth.getCurrentUser(), "Edited StatsRecords(EventID: %d) type to %s".formatted(eventid, value),
				ActionRecordType.EDIT);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@PostMapping("/statsmgr/edit_date")
	public ResponseEntity<String> statsManager_edit_date(@RequestParam int eventid, @RequestParam String value) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByEventId(eventid);
		if (stats.isEmpty()) {
			return ResponseEntity.status(404).body(Answer.fail("Event not found"));
		}
		stats.stream().forEach(stat -> stat.setDate(value.replaceAll("-", ".")));
		dbservice.getStatsRepository().saveAll(stats);
		rec.recordAction(auth.getCurrentUser(), "Edited StatsRecords(EventID: %d) date to %s".formatted(eventid, value),
				ActionRecordType.EDIT);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@PostMapping("/statsmgr/edit_group")
	public ResponseEntity<String> statsManager_edit_group(@RequestParam int eventid, @RequestParam String value) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByEventId(eventid);
		if (stats.isEmpty()) {
			return ResponseEntity.status(404).body(Answer.fail("Event not found"));
		}
		stats.stream().forEach(stat -> stat.setGroup(value));
		dbservice.getStatsRepository().saveAll(stats);
		rec.recordAction(auth.getCurrentUser(),
				"Edited StatsRecords(EventID: %d) group to %s".formatted(eventid, value), ActionRecordType.EDIT);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@GetMapping("/statsmgr/table")
	public String statsmgr_allTable(Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findAll(Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@PostMapping("/config/addevent")
	public String config_addevent(@RequestParam String event, @RequestParam String canSetReason,
			@RequestParam(required = false) String groupname) {
		if (groupname != null && !groupname.equals("null")
				&& dbservice.getGroupRepository().existsByName(groupname) == false)
			return "redirect:/admin?error_group";
		try {
			SettingsRepository.addData(SettingsRepository.SECTION_EVENT_TYPES,
					SettingsRepository.generateEventWithGroup(event, Boolean.parseBoolean(canSetReason), groupname));
			rec.recordAction(auth.getCurrentUser(), "Added event to config: %s".formatted(event),
					ActionRecordType.ADMIN);
			return "redirect:/admin?saved";
		} catch (ParseException e) {
			log.error("Error adding event. ", e);
			return "redirect:/admin?error_unknown";
		}
	}

	@PostMapping("/config/delevent")
	public String config_delevent(@RequestParam String event) {
		try {
			SettingsRepository.removeEvent(event);
			rec.recordAction(auth.getCurrentUser(), "Removed event from config: %s".formatted(event),
					ActionRecordType.ADMIN);
			return "redirect:/admin?deleted";
		} catch (Exception e) {
			log.error("Error removing event. ", e);
			return "redirect:/admin?error_unknown";
		}
	}

	@PostMapping("/config/addreason")
	public String config_addreason(@RequestParam String reason) {
		try {
			SettingsRepository.addData(SettingsRepository.SECTION_REASONS, SettingsRepository.generateReason(reason));
			rec.recordAction(auth.getCurrentUser(), "Added reason to config: %s".formatted(reason),
					ActionRecordType.ADMIN);
			return "redirect:/admin?saved";
		} catch (ParseException e) {
			log.error("Error adding reason. ", e);
			return "redirect:/admin?error_unknown";
		}
	}

	@PostMapping("/config/delreason")
	public String config_delreason(@RequestParam String reason) {
		try {
			SettingsRepository.removeReason(reason);
			rec.recordAction(auth.getCurrentUser(), "Removed reason from config: %s".formatted(reason),
					ActionRecordType.ADMIN);
			return "redirect:/admin?deleted";
		} catch (Exception e) {
			log.error("Error removing reason. ", e);
			return "redirect:/admin?error_unknown";
		}
	}

	@PostMapping("/config/addreplacement")
	public String config_addreplacement(@RequestParam String from, @RequestParam String to) {
		try {
			SettingsRepository.addData(SettingsRepository.SECTION_REPLACEMENTS,
					SettingsRepository.generateReplacement(from, to));
			rec.recordAction(auth.getCurrentUser(), "Added replacement to config: from %s to %s".formatted(from, to),
					ActionRecordType.ADMIN);
			return "redirect:/admin?saved";
		} catch (ParseException e) {
			log.error("Error adding replacement. ", e);
			return "redirect:/admin?error_unknown";
		}
	}

	@PostMapping("/config/delreplacement")
	public String config_delreplacement(@RequestParam String from) {
		try {
			SettingsRepository.removeReplacement(from);
			rec.recordAction(auth.getCurrentUser(), "Removed replacement from config: from %s".formatted(from),
					ActionRecordType.ADMIN);
			return "redirect:/admin?deleted";
		} catch (Exception e) {
			log.error("Error removing replacement. ", e);
			return "redirect:/admin?error_unknown";
		}
	}

	/*
	 * Telegram
	 */
	@GetMapping("/chatsmgr")
	public String chatsManager(Model model) {
		model.addAttribute("users",
				dbservice.getUserRepository().findAll().stream().filter(user -> user.getTelegram() == null).toList());
		model.addAttribute("chats", chatRep.findAll());
		return "admin/chatsmgr";
	}

	@PostMapping("/chatsmgr/connect")
	public ResponseEntity<String> chatsManager_connect(@RequestParam Long id, @RequestParam int userid) {
		Optional<User> optUser = dbservice.getUserRepository().findById(userid);
		if (optUser.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("User not found"));
		Optional<Chat> optChat = chatRep.findById(id);
		if (optChat.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Chat not found"));
		User user = optUser.get();
		Chat chat = optChat.get();
		chat.setOwner(user);
		chatRep.save(chat);
		botServ.sendPreparedMessage("registered", chat);
		rec.recordAction(auth.getCurrentUser(), "Connected Telegram Chat (id: %d) to %d".formatted(id, userid),
				ActionRecordType.TELEGRAM);
		return ResponseEntity.ok(Answer.success());
	}

	@PostMapping("/chatsmgr/disconnect")
	public ResponseEntity<String> chatsManager_disconnect(@RequestParam Long id) {
		Optional<Chat> chat1 = chatRep.findById(id);
		if (chat1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("Chat not found"));
		Chat chat = chat1.get();
		if (chat.getOwner() == null)
			return ResponseEntity.status(404).body(Answer.fail("User has not been connected before"));
		botServ.sendPreparedMessage("parting", chat);
		User user = chat.getOwner();
		user.setTelegram(null);
		chat.setOwner(null);
		chatRep.save(chat);
		dbservice.getUserRepository().save(user);
		rec.recordAction(auth.getCurrentUser(), "Disconnected Telegram Chat Id: %d".formatted(id),
				ActionRecordType.TELEGRAM);
		return ResponseEntity.ok(Answer.success());
	}

	@PostMapping("/chatsmgr/delete")
	public ResponseEntity<String> chatsManager_remove(@RequestParam Long id) {
		chatRep.deleteById(id);
		rec.recordAction(auth.getCurrentUser(), "Removed Telegram Chat Id: %d".formatted(id), ActionRecordType.REMOVE);
		return ResponseEntity.ok(Answer.success());
	}

	@GetMapping("/journal")
	public String journal(Model model) {
		model.addAttribute("records", rec.getActionLog().findAll(Sort.by(Sort.Direction.DESC, "id")));
		return "admin/journal";
	}

	@GetMapping("/session-check")
	public ResponseEntity<String> checkTimeout(HttpSession session) {
		return ResponseEntity.ok("Max session interval is  " + session.getMaxInactiveInterval() + " seconds");
	}
}
