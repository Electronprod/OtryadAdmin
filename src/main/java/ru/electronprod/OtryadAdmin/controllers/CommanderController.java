package ru.electronprod.OtryadAdmin.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.MarkDTO;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.services.StatsHandler;
import ru.electronprod.OtryadAdmin.utils.Answer;

@Slf4j
@Controller
@RequestMapping("/commander")
@PreAuthorize("hasAuthority('ROLE_COMMANDER')")
public class CommanderController {
	@Autowired
	private StatsHandler statsWorker;
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;

	@GetMapping("")
	public String overview(Model model) {
		User user = dbservice.getUserRepository().findById(authHelper.getCurrentUser().getId()).orElseThrow();
		model.addAttribute("user", user);
		model.addAttribute("groups", user.getGroups().stream().filter(gr -> gr.isEditable()).toList());
		model.addAttribute("reasons_for_absences_map", SettingsRepository.getReasons_for_absences());
		model.addAttribute("humanList",
				dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "commander/mark";
	}

	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody MarkDTO dto) {
		try {
			Optional<Group> group = dbservice.getGroupRepository().findById(dto.getGroupID());
			List<Human> people = null;
			if (group.isPresent()) {
				people = group.get().getHumans().stream().collect(Collectors.toCollection(ArrayList::new));
			} else {
				people = dbservice.getHumanRepository().findAll();
			}
			int event_id = statsWorker.mark_group(dto, authHelper.getCurrentUser(), people,
					group.isPresent() ? group.get().getName() : null);
			return ResponseEntity.accepted().body(Answer.marked(event_id));
		} catch (Exception e) {
			log.error("Mark error (commander.mark):", e);
			return ResponseEntity.internalServerError().body(Answer.fail(e.getMessage()));
		}
	}

	@GetMapping("/stats")
	public String stats_overview(Model model) {
		var user = authHelper.getCurrentUser();
		model.addAttribute("groups", dbservice.getGroupRepository().findByMarker(user));
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		model.addAttribute("events", dbservice.getStatsRepository()
				.findDistinctTypesAuthorWithoutGroups(user.getLogin()).stream().sorted().toList());
		var stats = dbservice.getStatsRepository().findByAuthor(user.getLogin());
		model.addAttribute("reasons",
				stats.stream().map(StatsRecord::getReason)
						.filter(reason -> !SettingsRepository.getReplacements().containsKey(reason))
						.collect(Collectors.groupingBy(reason -> reason, Collectors.counting())));
		return "commander/stats_overview";
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, @RequestParam Optional<String> eventid,
			@RequestParam Optional<String> group, Model model) {
		try {
			var data = dbservice.getStatsRepository().findByDateAndAuthor(DBService.getStringDate(date),
					authHelper.getCurrentUser().getLogin(), Sort.by(Sort.Direction.DESC, "id"));
			if (eventid.isPresent())
				data = data.stream().filter(e -> String.valueOf(e.getEvent_id()).equals(eventid.get())).toList();
			if (group.isPresent())
				data = data.stream().filter(e -> String.valueOf(e.getGroup()).equals(group.get())).toList();
			model.addAttribute("statss", data);
		} catch (Exception e) {
		}
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/table")
	public String stats_allTable(Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByAuthor(authHelper.getCurrentUser().getLogin(),
				Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String stats_personal() {
		return "forward:/commander/stats/personal_id";
	}

	@GetMapping("/stats/personal/table")
	public String personalStatsTable(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/commander/stats?error_notfound";
		}
		model.addAttribute("statss", dbservice.getStatsRepository().findByHumanAndAuthor(human.get(), user.getLogin(),
				Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal_id")
	public String stats_personal(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/commander/stats?error_notfound";
		}
		statsWorker.getMainPersonalReportModel(dbservice.getStatsRepository().findByHuman(human.get()), model, true);
		model.addAttribute("person", human.get().getLastname() + " " + human.get().getName());
		model.addAttribute("human_id", human.get().getId());
		return "commander/personal_stats";
	}

	@GetMapping("/stats/report")
	public String stats_forEvent(@RequestParam String event_name, Model model) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByTypeAndAuthor(event_name,
				authHelper.getCurrentUser().getLogin());
		model.addAttribute("data", statsWorker.getEventReport(stats));
		model.addAttribute("eventName", event_name);
		return "public/event_stats";
	}

	@GetMapping("/stats/report/table")
	public String stats_eventTable(@RequestParam String event_name, Model model) {
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByTypeAndAuthor(event_name, authHelper.getCurrentUser().getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/group/{id}")
	public String stats_group_overview(@PathVariable("id") int id, Model model) {
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (group.isEmpty())
			return "redirect:/commander/stats?error_notfound";
		model.addAttribute("group", group.get());
		model.addAttribute("humans",
				group.get().getHumans().stream().sorted(Comparator.comparing(obj -> obj.getLastname())).toList());
		model.addAttribute("group_events",
				dbservice.getStatsRepository().findDistinctTypesGroup(group.get().getName()));
		model.addAttribute("reasons",
				dbservice.getStatsRepository().findByGroup(group.get().getName(), Sort.by(Sort.Direction.ASC, "reason"))
						.stream().map(StatsRecord::getReason)
						.filter(reason -> !SettingsRepository.getReplacements().containsKey(reason))
						.collect(Collectors.groupingBy(reason -> reason, Collectors.counting())));
		return "commander/groupstats/stats_overview";
	}

	@GetMapping("/stats/group/report")
	public String stats_group_report(@RequestParam String eventname, @RequestParam String groupname, Model model) {
		model.addAttribute("data", statsWorker.getEventReport(dbservice.getStatsRepository()
				.findByTypeAndGroup(eventname, groupname, Sort.by(Sort.Direction.DESC, "date"))));
		model.addAttribute("eventName", eventname);
		return "public/event_stats";
	}

	@GetMapping("/stats/group/report/table")
	public String stats_group_report_table(@RequestParam String eventname, @RequestParam String groupname,
			Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByTypeAndGroup(eventname, groupname,
				Sort.by(Sort.Direction.DESC, "date")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/group/all_report")
	public String stats_group_report(@RequestParam String groupname, Model model) {
		model.addAttribute("data", statsWorker.getEventReport(
				dbservice.getStatsRepository().findByGroup(groupname, Sort.by(Sort.Direction.DESC, "date"))));
		model.addAttribute("eventName", groupname);
		return "public/event_stats";
	}

	@GetMapping("/stats/group/all_table")
	public String stats_group_allTable(@RequestParam String groupname, Model model) {
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByGroup(groupname, Sort.by(Sort.Direction.DESC, "date")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/show_never_marked")
	public String shownevm(Model model) {
		List<Human> h = dbservice.getHumanRepository().findAll();
		h.removeAll(dbservice.getStatsRepository().findDistinctHumansAuthor(authHelper.getCurrentUser().getLogin()));
		model.addAttribute("humans", h);
		return "public/humans_rawtable";
	}

	@GetMapping("/stats/report_me")
	public String stats_report_me(Model model) {
		var user = authHelper.getCurrentUser();
		model.addAttribute("data",
				statsWorker.getEventReport(dbservice.getStatsRepository().findByAuthor(user.getLogin())));
		model.addAttribute("eventName", user.getName());
		return "public/event_stats";
	}

	@GetMapping("/humans")
	public String getHumansData(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "public/humans_rawtable";
	}
}
