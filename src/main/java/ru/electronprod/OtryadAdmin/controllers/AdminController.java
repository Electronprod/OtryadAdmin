package ru.electronprod.OtryadAdmin.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.SquadStats;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.HumanHelper;
import ru.electronprod.OtryadAdmin.models.dto.SquadHelper;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.AdminService;
import ru.electronprod.OtryadAdmin.utils.FileOptions;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
	@Autowired
	private AdminService adminService;
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper auth;

	/*
	 * Main page
	 */
	@GetMapping("")
	public String dash(Model model) {
		double processCpuLoad = adminService.getSystemInfoBean().getProcessCpuLoad();
		double cpuLoad = adminService.getSystemInfoBean().getCpuLoad();
		model.addAttribute("cpu", String.format("%.2f%% / %.2f%%", processCpuLoad * 100, cpuLoad * 100));
		long committedMemory = adminService.getSystemInfoBean().getCommittedVirtualMemorySize();
		long freeMemory = adminService.getSystemInfoBean().getFreeMemorySize();
		long totalMemory = adminService.getSystemInfoBean().getTotalMemorySize();
		model.addAttribute("memory", String.format("%.2f GB / %d MB / %d MB",
				committedMemory / (1024.0 * 1024.0 * 1024.0), freeMemory / (1024 * 1024), totalMemory / (1024 * 1024)));

		double freePhysicalMemory = adminService.getFreeDiskSpace();
		double usablePhysicalMemory = adminService.getUsableDiskSpace();
		double totalPhysicalMemory = adminService.getTotalDiskSpace();
		model.addAttribute("disk", String.format("%.2f GB / %.2f GB / %.2f GB", freePhysicalMemory,
				usablePhysicalMemory, totalPhysicalMemory));

		return "admin/dashboard";
	}

	/*
	 * User manager
	 */
	@GetMapping("/usermgr")
	public String usermgr(Model model) {
		model.addAttribute("users", dbservice.getUserRepository().findAll());
		return "admin/usermgr/usermgr";
	}

	@GetMapping("/usermgr/add")
	public String userManager_add(Model model) {
		model.addAttribute("user", new User());
		return "admin/usermgr/usermgr_add";
	}

	@PostMapping("/usermgr/add")
	public String userManager_addAction(@ModelAttribute("user") User user) {
		if (user.getTelegram().equalsIgnoreCase("null")) {
			user.setTelegram(null);
		}
		auth.register(user);
		return "redirect:/admin/usermgr?saved";
	}

	@GetMapping("/usermgr/edit")
	public String userManager_edit(@RequestParam() int id, Model model) {
		model.addAttribute("id", id);
		return "admin/usermgr/usermgr_edit";
	}

	@GetMapping("/usermgr/setrole")
	public String userManager_setRole(@RequestParam() int id, @RequestParam() String role) {
		Optional<User> user = dbservice.getUserRepository().findById(id);
		if (user.isEmpty()) {
			return "redirect:/admin/usermgr?error";
		}
		User usr = user.get();
		usr.setRole(role);
		dbservice.getUserRepository().save(usr);
		return "redirect:/admin/usermgr?saved";
	}

	@GetMapping("/usermgr/setlogin")
	public String userManager_setLogin(@RequestParam() int id, @RequestParam() String login) {
		Optional<User> user = dbservice.getUserRepository().findById(id);
		if (user.isEmpty()) {
			return "redirect:/admin/usermgr?error";
		}
		User usr = user.get();
		usr.setLogin(login);
		dbservice.getUserRepository().save(usr);
		return "redirect:/admin/usermgr?saved";
	}

	@GetMapping("/usermgr/setpassword")
	public String userManager_setPassword(@RequestParam() int id, @RequestParam() String password) {
		Optional<User> user = dbservice.getUserRepository().findById(id);
		if (user.isEmpty()) {
			return "redirect:/admin/usermgr?error";
		}
		User usr = user.get();
		usr.setPassword(password);
		auth.register(usr);
		return "redirect:/admin/usermgr?saved";
	}

	@GetMapping("/usermgr/delete")
	public String userManager_delete(@RequestParam() int id) {
		Optional<User> user = dbservice.getUserRepository().findById(id);
		if (user.isEmpty()) {
			return "redirect:/admin/usermgr?error";
		}
		if (adminService.isNativeAdmin(user.get())) {
			return "redirect:/admin/usermgr?error_protected";
		}
		dbservice.getUserRepository().deleteById(id);
		return "redirect:/admin/usermgr?deleted";
	}

	/*
	 * Squad manager
	 */
	@GetMapping("/squadmgr")
	public String squadManager(Model model) {
		model.addAttribute("squads", dbservice.getSquadRepository().findAll());
		return "admin/squadmgr/squadmgr";
	}

	// id - from usermgr
	@GetMapping("/squadmgr/add")
	public String squadManager_add(@RequestParam() int id, Model model) {
		Optional<User> usr = dbservice.getUserRepository().findById(id);
		if (usr.isEmpty() || !usr.get().getRole().equals("ROLE_SQUADCOMMANDER")) {
			return "redirect:/admin/squadmgr?error";
		}
		if (usr.get().getSquad() != null) {
			return "redirect:/admin/squadmgr?error_hasvalue";
		}
		SquadHelper squadHelper = new SquadHelper();
		squadHelper.setCommander_id(id);
		squadHelper.setCommanderName(usr.get().getLogin());
		model.addAttribute("squadHelper", squadHelper);
		return "admin/squadmgr/squadmgr_add";
	}

	@PostMapping("/squadmgr/add")
	public String squadManager_addAction(@ModelAttribute("squadHelper") SquadHelper squadHelper) {
		Squad squad = new Squad();
		squad.setSquadName(squadHelper.getSquadName());
		squad.setCommanderName(squadHelper.getCommanderName());
		squad.setCommander(dbservice.getUserRepository().findById(squadHelper.getCommander_id()).orElseThrow());
		dbservice.getSquadRepository().save(squad);
		return "redirect:/admin/squadmgr?saved";
	}

	@GetMapping("/squadmgr/edit")
	public String squadManager_edit(@RequestParam() int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/admin/squadmgr?error";
		SquadHelper squadHelper = new SquadHelper();
		squadHelper.setCommanderName(squad.get().getCommanderName());
		squadHelper.setCommander_id(squad.get().getCommander().getId());
		squadHelper.setSquadName(squad.get().getSquadName());
		squadHelper.setId(id);
		model.addAttribute("squadHelper", squadHelper);
		return "admin/squadmgr/squadmgr_edit";
	}

	@PostMapping("/squadmgr/edit")
	public String squadManager_edit(@ModelAttribute("squad") SquadHelper squadHelper) {
		Squad squad = dbservice.getSquadRepository().findById(squadHelper.getId()).orElseThrow();
		squad.setCommanderName(squadHelper.getCommanderName());
		squad.setSquadName(squadHelper.getSquadName());
		Optional<User> usr = dbservice.getUserRepository().findById(squadHelper.getCommander_id());
		if (usr.isEmpty() || !usr.get().getRole().equals("ROLE_SQUADCOMMANDER")) {
			return "redirect:/admin/squadmgr?error";
		}
		if (usr.get().getSquad() == null) {
			return "redirect:/admin/squadmgr?error";
		}
		squad.setCommander(usr.get());
		dbservice.getSquadRepository().save(squad);
		return "redirect:/admin/squadmgr?edited";
	}

	@GetMapping("/squadmgr/delete")
	public String squadManager_delete(@RequestParam() int id) {
		if (dbservice.getSquadRepository().findById(id).isEmpty()) {
			return "redirect:/admin/squadmgr?error";
		}
		dbservice.getSquadRepository().deleteById(id);
		return "redirect:/admin/squadmgr?deleted";
	}

	/*
	 * Human manager
	 */
	@GetMapping("/humanmgr")
	public String humanManager(Model model) {
		List<Human> humans = dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname"));
		model.addAttribute("humans", humans);
		return "admin/humanmgr/humanmgr";
	}

	// id - squad_id
	@GetMapping("/humanmgr/add")
	public String humanManager_add(@RequestParam() int id, Model model) {
		// Checking squad existence
		if (dbservice.getSquadRepository().findById(id).isEmpty())
			return "redirect:/admin/humanmgr?errorsquad";

		HumanHelper helper = new HumanHelper();
		helper.setSquad_id(id);
		model.addAttribute("humanHelper", helper);
		return "admin/humanmgr/humanmgr_add";
	}

	@PostMapping("/humanmgr/add")
	public String humanManager_addAction(@ModelAttribute("humanHelper") HumanHelper helper) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(helper.getSquad_id());
		// Checking existence
		if (squad.isEmpty())
			return "redirect:/admin/humanmgr?errorsquad";

		// Generating human object
		Human human = HumanHelper.fillDefaultValues(helper);
		human.setSquad(squad.orElseThrow());
		// Saving to DB
		dbservice.getHumanRepository().save(human);
		return "redirect:/admin/humanmgr?saved";
	}

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

	@GetMapping("/humanmgr/delete")
	public String humanManager_delete(@RequestParam() int id) {
		if (dbservice.getHumanRepository().findById(id).isEmpty()) {
			return "redirect:/admin/humanmgr?error_notfound";
		}
		dbservice.getHumanRepository().deleteById(id);
		return "redirect:/admin/humanmgr?deleted";
	}

	@GetMapping("/humanmgr/deleteall")
	public String humanManager_deleteAll() {
		dbservice.getHumanRepository().deleteAll();
		return "redirect:/admin/humanmgr?deleted";
	}

	@GetMapping("/humanmgr/edit")
	public String humanManager_edit(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		// Checking existence (Human)
		if (human.isEmpty())
			return "redirect:/admin/humanmgr?error_notfound";
		HumanHelper helper = HumanHelper.fillDefaultValues(human.get());
		helper.setSquad_id(human.get().getSquad().getId());
		model.addAttribute("humanHelper", helper);
		return "admin/humanmgr/humanmgr_edit";
	}

	@PostMapping("/humanmgr/edit")
	public String humanManager_editAction(@ModelAttribute("humanHelper") HumanHelper helper) {
		Optional<Human> human1 = dbservice.getHumanRepository().findById(helper.getId());
		// Checking existence (Human)
		if (human1.isEmpty())
			return "redirect:/admin/humanmgr?error_notfound";
		Human human = human1.get();
		// If Squad changed
		if (human.getSquad().getId() != helper.getSquad_id()) {
			// Checking existence (new Squad)
			Optional<Squad> squad = dbservice.getSquadRepository().findById(helper.getSquad_id());
			if (squad.isEmpty())
				return "redirect:/admin/humanmgr?errorsquad";
			// Deleting human from old squad
			Squad oldsquad = dbservice.getSquadRepository().findById(human.getSquad().getId()).orElseThrow();
			oldsquad.getHumans().remove(human);
			dbservice.getSquadRepository().save(oldsquad);
			// Adding to new squad
			Squad newSquad = squad.get();
			human.setSquad(newSquad);
			newSquad.getHumans().add(human);
			dbservice.getSquadRepository().save(newSquad);
		}
		// Updating default values
		human = HumanHelper.fillDefaultValues(helper, human);
		dbservice.getHumanRepository().save(human);
		return "redirect:/admin/humanmgr?edited";
	}

	/*
	 * Stats manager
	 */
	@GetMapping("/statsmgr")
	public String statsManager(Model model) {
		model.addAttribute("event_types_map", SettingsRepository.getEvent_types());
		model.addAttribute("reasons_for_absences_map", SettingsRepository.getReasons_for_absences());
		return "admin/statsmgr/statsmgr";
	}

	@GetMapping("/statsmgr/table")
	public String statsManager_table(Model model) {
		return "forward:/observer/stats/table";
	}

	@GetMapping("/statsmgr/delete_event")
	public String statsManager_delete_byEventID(@RequestParam int id) {
		List<SquadStats> stats = dbservice.getStatsRepository().findByEventId(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		dbservice.getStatsRepository().deleteAll(stats);
		return "redirect:/admin/statsmgr?deleted";
	}

	@GetMapping("/statsmgr/delete")
	public String statsManager_delete_byID(@RequestParam int id) {
		Optional<SquadStats> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		dbservice.getStatsRepository().delete(stats.get());
		return "redirect:/admin/statsmgr?deleted";
	}

	@PostMapping("/statsmgr/edit_type")
	public String statsManager_edit_type(@RequestParam int eventid, @RequestParam String statsType) {
		List<SquadStats> stats = dbservice.getStatsRepository().findByEventId(eventid);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.stream().forEach(stat -> stat.setType(statsType));
		dbservice.getStatsRepository().saveAll(stats);
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_type_single")
	public String statsManager_edit_type_single(@RequestParam int id, @RequestParam String statsType) {
		Optional<SquadStats> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.get().setType(statsType);
		dbservice.getStatsRepository().save(stats.get());
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_date")
	public String statsManager_edit_date(@RequestParam int eventid, @RequestParam String date) {
		List<SquadStats> stats = dbservice.getStatsRepository().findByEventId(eventid);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.stream().forEach(stat -> stat.setDate(date.replaceAll("-", ".")));
		dbservice.getStatsRepository().saveAll(stats);
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_date_single")
	public String statsManager_edit_date_single(@RequestParam int id, @RequestParam String date) {
		Optional<SquadStats> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.get().setDate(date.replaceAll("-", "."));
		dbservice.getStatsRepository().save(stats.get());
		return "redirect:/admin/statsmgr?edited";
	}

	@PostMapping("/statsmgr/edit_reason")
	public String statsManager_edit_reason(@RequestParam int eventid, @RequestParam String reason) {
		List<SquadStats> statsList = dbservice.getStatsRepository().findByEventId(eventid);
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
		Optional<SquadStats> stats = dbservice.getStatsRepository().findById(id);
		if (stats.isEmpty()) {
			return "redirect:/admin/statsmgr?error_notfound";
		}
		stats.get().setReason(reason);
		dbservice.getStatsRepository().save(stats.get());
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
}
