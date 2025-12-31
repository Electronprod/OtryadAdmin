package ru.electronprod.OtryadAdmin.controllers;

import java.util.ArrayList;
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
import ru.electronprod.OtryadAdmin.models.ActionRecordType;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.MarkDTO;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.services.MarkService;
import ru.electronprod.OtryadAdmin.services.StatsProcessor;
import ru.electronprod.OtryadAdmin.utils.Answer;

@Slf4j
@Controller
@RequestMapping("/commander")
@PreAuthorize("hasAuthority('ROLE_COMMANDER')")
public class CommanderController {
	@Autowired
	private MarkService markService;
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;
	@Autowired
	private StatsProcessor statsProcessor;
	

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
		var user = authHelper.getCurrentUser();
		try {
			Optional<Group> group = dbservice.getGroupRepository().findById(dto.getGroupID());
			List<Human> people = null;
			if (group.isPresent()) {
				people = group.get().getHumans().stream().collect(Collectors.toCollection(ArrayList::new));
			} else {
				people = dbservice.getHumanRepository().findAll();
			}
			int event_id = markService.mark_group(dto, user, people, group.isPresent() ? group.get().getName() : null);
			return ResponseEntity.accepted().body(Answer.marked(event_id));
		} catch (Exception e) {
			log.error("Mark error (commander.mark):", e);
			dbservice.recordAction(user, e.getMessage(), ActionRecordType.MARK_EXCEPTION);
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
		return statsProcessor.getEventByDateAndFilters_user(authHelper.getCurrentUser(), date, eventid, group, model);
	}

	@GetMapping("/stats/table")
	public String stats_allMarksTable(Model model) {
		return statsProcessor.showFullTable_user(authHelper.getCurrentUser(), model);
	}

	@GetMapping("/stats/personal")
	public String stats_personal() {
		return "forward:/commander/stats/personal_id";
	}

	@GetMapping("/stats/personal/table")
	public String personalStatsTable(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/commander/stats?error_notfound";
		}
		return statsProcessor.getTableForPerson_user(authHelper.getCurrentUser(), human.get(), model);
	}

	@GetMapping("/stats/personal_id")
	public String stats_personal(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/commander/stats?error_notfound";
		}
		return statsProcessor.getPersonReport(human.get(), model);
	}

	@GetMapping("/stats/report")
	public String stats_forEvent(@RequestParam String event_name, Model model) {
		return statsProcessor.reportEvent_user(authHelper.getCurrentUser(), event_name, model);
	}

	@GetMapping("/stats/report/table")
	public String stats_eventTable(@RequestParam String event_name, Model model) {
		return statsProcessor.showEventTable_user(authHelper.getCurrentUser(), event_name, model);
	}

	@GetMapping("/stats/group/{id}")
	public String stats_group_overview(@PathVariable("id") int id, Model model) {
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (group.isEmpty())
			return "redirect:/commander/stats?error_notfound";
		statsProcessor.fill_statsPage_group(group.get(), model);
		return "commander/groupstats/stats_overview";
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

	@GetMapping("/stats/show_never_marked")
	public String shownevm(Model model) {
		List<Human> h = dbservice.getHumanRepository().findAll();
		h.removeAll(dbservice.getStatsRepository().findDistinctHumansAuthor(authHelper.getCurrentUser().getLogin()));
		model.addAttribute("humans", h);
		return "public/humans_rawtable";
	}

	@GetMapping("/stats/report_me")
	public String stats_report_me(Model model) {
		return statsProcessor.report_user(authHelper.getCurrentUser(), true, model);
	}

	@GetMapping("/humans")
	public String getHumansData() {
		return "forward:/observer/data";
	}
}
