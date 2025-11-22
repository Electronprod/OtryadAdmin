package ru.electronprod.OtryadAdmin.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.Chat;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.services.StatsProcessor;
import ru.electronprod.OtryadAdmin.telegram.BotService;
import ru.electronprod.OtryadAdmin.utils.Answer;

@Controller
@RequestMapping("/observer")
@PreAuthorize("hasAuthority('ROLE_OBSERVER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_COMMANDER')")
@Slf4j
public class ObserverController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper auth;
	@Autowired
	private BotService botServ;
	@Autowired
	private StatsProcessor statsProcessor;

	@GetMapping("")
	public String stats_overview(Model model) {
		model.addAttribute("name", auth.getCurrentUser().getName());
		model.addAttribute("user_role", auth.getCurrentUser().getRole());
		// Squads view
		model.addAttribute("squadList", dbservice.getSquadRepository().findAll());
		// Groups view
		model.addAttribute("all_groups", dbservice.getGroupRepository().findAll());
		// Other info
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		model.addAttribute("people_size", dbservice.getHumanRepository().getSize());
		model.addAttribute("people_missed", dbservice.getStatsRepository().countByIsPresent(false));
		model.addAttribute("people_attended", dbservice.getStatsRepository().countByIsPresent(true));
		model.addAttribute("people_missed_today",
				dbservice.getStatsRepository().countByDateAndIsPresent(DBService.getStringDate(), false));
		model.addAttribute("people_attended_today",
				dbservice.getStatsRepository().countByDateAndIsPresent(DBService.getStringDate(), true));
		model.addAttribute("commanders_marked_today",
				dbservice.getStatsRepository().countDistinctAuthorsByDate(DBService.getStringDate()));
		model.addAttribute("events",
				dbservice.getStatsRepository().findDistinctTypesWithoutGroups().stream().sorted().toList());
		return "observer/stats_overview";
	}

	@GetMapping("/stats")
	public String overview() {
		return "forward:/observer";
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, Model model) throws Exception {
		model.addAttribute("statss", dbservice.getStatsRepository().findByDate(DBService.getStringDate(date),
				Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/table")
	public String stats_allTable(Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findAll(Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/report")
	public String stats_event(@RequestParam String event_name, Model model) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByType(event_name);
		model.addAttribute("data", statsProcessor.getAttendanceReportByStatsRecords(stats));
		model.addAttribute("eventName", event_name);
		return "public/event_stats";
	}

	@GetMapping("/stats/event_table")
	public String stats_eventTable(@RequestParam String event_name, Model model) {
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByType(event_name, Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String stats_personal() {
		return "forward:/observer/stats/personal_id";
	}

	@GetMapping("/stats/personal/table")
	public String stats_personalTable_old() {
		return "forward:/observer/stats/personal/table_id";
	}

	@GetMapping("/stats/personal_id")
	public String stats_personal(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/observer/stats?error_notfound";
		}
		return statsProcessor.getPersonReport(human.get(), model);
	}

	@GetMapping("/stats/personal/table_id")
	public String stats_personalTable(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/observer/stats?error_notfound";
		}
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByHuman(human.get(), Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	/*
	 * Squad pages
	 */
	@GetMapping("/stats/squad/{id}")
	public String stats_squad_overview(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		var user = squad.get().getCommander();
		model.addAttribute("name", user.getName());
		model.addAttribute("login", user.getLogin());
		model.addAttribute("humans",
				squad.get().getHumans().stream().sorted(Comparator.comparing(obj -> obj.getLastname())).toList());
		var stats = dbservice.getStatsRepository().findByAuthor(user.getLogin());
		model.addAttribute("reasons",
				stats.stream().map(StatsRecord::getReason)
						.filter(reason -> !SettingsRepository.getReplacements().containsKey(reason))
						.collect(Collectors.groupingBy(reason -> reason, Collectors.counting())));
		model.addAttribute("events",
				stats.stream().map(StatsRecord::getType).distinct().sorted().collect(Collectors.toList()));
		return "observer/squadstats/stats_overview";
	}

	@GetMapping("/stats/squad/{id}/date")
	public String stats_squad_byDate(@PathVariable("id") int id, @RequestParam String date,
			@RequestParam Optional<String> eventid, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		return statsProcessor.getEventByDateAndFilters_user(squad.get().getCommander(), DBService.getStringDate(date),
				eventid, Optional.empty(), model);
	}

	@GetMapping("/stats/squad/{id}/report")
	public String stats_squad_report(@PathVariable("id") int id, @RequestParam String event_name, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		return statsProcessor.reportEvent_user(squad.get().getCommander(), event_name, model);
	}

	@GetMapping("/stats/squad/{id}/event_table")
	public String stats_squad_event_table(@PathVariable("id") int id, @RequestParam String event_name, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		return statsProcessor.showEventTable_user(squad.get().getCommander(), event_name, model);
	}

	@GetMapping("/stats/squad/{id}/table")
	public String stats_squad_allTable(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		return statsProcessor.showFullTable_user(squad.get().getCommander(), model);
	}

	/*
	 * Group pages
	 */
	@GetMapping("/stats/group/{id}")
	public String stats_group_overview(@PathVariable("id") int id, Model model) {
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (group.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		statsProcessor.fill_statsPage_group(group.get(), model);
		return "observer/groupstats/stats_overview";
	}

	@GetMapping("/stats/group/date")
	public String stats_group_date(@RequestParam String date, @RequestParam String groupname, Model model)
			throws Exception {
		model.addAttribute("statss",
				dbservice.getStatsRepository()
						.findByDate(DBService.getStringDate(date), Sort.by(Sort.Direction.DESC, "id")).stream()
						.filter(s -> s.getGroup() != null && s.getGroup().equals(groupname)));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/group/report")
	public String stats_group_report(@RequestParam String eventname, @RequestParam String groupname, Model model) {
		return statsProcessor.reportGroupEvent(eventname, groupname, model);
	}

	@GetMapping("/stats/group/report/table")
	public String stats_group_report_table(@RequestParam String eventname, @RequestParam String groupname,
			Model model) {
		return statsProcessor.showGroupEventTable(eventname, groupname, model);
	}

	@GetMapping("/stats/group/all_report")
	public String stats_group_report(@RequestParam String groupname, Model model) {
		return statsProcessor.reportGroup(groupname, model);
	}

	@GetMapping("/stats/group/all_table")
	public String stats_group_allTable(@RequestParam String groupname, Model model) {
		return statsProcessor.showGroupTable(groupname, model);
	}

	@GetMapping("/data")
	public String humans_data(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "public/humans_rawtable";
	}

	@GetMapping("/demand")
	public String telegram(Model model) {
		model.addAttribute("users",
				dbservice.getUserRepository().findAll().stream().filter(user -> user.getTelegram() != null).toList());
		return "observer/demand_marks";
	}

	@PostMapping("/telegram/sendremind")
	public ResponseEntity<String> telegram_sendremind(@RequestParam String eventname, @RequestParam String description,
			int userid) {
		Optional<User> optUser = dbservice.getUserRepository().findById(userid);
		if (optUser.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("User not found"));
		Chat chat = optUser.get().getTelegram();
		if (chat != null) {
			botServ.sendSignedReminder(eventname, description, auth.getCurrentUser().getName(), chat);
			return ResponseEntity.ok(Answer.success());
		}
		return ResponseEntity.status(404).body(Answer.fail("Error finding user's telegram."));
	}

	@PostMapping("/telegram/sendmessage")
	public ResponseEntity<String> telegram_sendmessage(@RequestParam String message, int userid) {
		Optional<User> optUser = dbservice.getUserRepository().findById(userid);
		if (optUser.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("User not found"));
		Chat chat = optUser.get().getTelegram();
		if (chat != null) {
			botServ.sendSignedMessage(message, auth.getCurrentUser().getName(), chat);
			return ResponseEntity.ok(Answer.success());
		}
		return ResponseEntity.status(404).body(Answer.fail("Error finding user's telegram."));
	}
}
