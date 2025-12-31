package ru.electronprod.OtryadAdmin.controllers;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.models.*;
import ru.electronprod.OtryadAdmin.models.dto.MarkDTO;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.services.MarkService;
import ru.electronprod.OtryadAdmin.services.StatsProcessor;
import ru.electronprod.OtryadAdmin.utils.Answer;
import ru.electronprod.OtryadAdmin.utils.NormalException;

@Slf4j
@Controller
@RequestMapping("/squadcommander")
@PreAuthorize("hasAuthority('ROLE_SQUADCOMMANDER')")
public class SquadCommanderController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;
	@Autowired
	private MarkService markService;
	@Autowired
	private StatsProcessor statsProcessor;

	@GetMapping("")
	public String overview() {
		// A placeholder for the future
		return "forward:/squadcommander/mark";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("humanList", dbservice.getSquadRepository().findByCommander(user).getHumans().stream()
				.sorted(Comparator.comparing(Human::getLastname)).collect(Collectors.toList()));
		model.addAttribute("reasons_for_absences_map", SettingsRepository.getReasons_for_absences());
		model.addAttribute("event_types_map", SettingsRepository.getEvent_types());
		model.addAttribute("user_name", user.getName());
		return "squadcommander/mark";
	}

	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody MarkDTO dto) {
		var user = authHelper.getCurrentUser();
		try {
			var humans = dbservice.getSquadRepository().findByCommander(user).getHumans();
			String group = null;
			if (dto.getGroupID() != -1) {
				Optional<Group> groupObj = dbservice.getGroupRepository().findById(dto.getGroupID());
				if (groupObj.isPresent()) {
					humans.retainAll(groupObj.get().getHumans());
					group = groupObj.get().getName();
				} else {
					log.warn("(squadcommander.mark): incorrect group. Please, check front-end side. GroupID received: "
							+ dto.getGroupID());
				}
			}
			int event_id = markService.mark_group(dto, user, humans, group);
			return ResponseEntity.accepted().body(Answer.marked(event_id));
		} catch (Exception e) {
			log.error("Mark error (squadcommander.mark):", e);
			dbservice.recordAction(user, e.getMessage(), ActionRecordType.MARK_EXCEPTION);
			return ResponseEntity.internalServerError().body(Answer.fail(e.getMessage()));
		}
	}

	@GetMapping("/stats")
	public String stats_overview(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("humans",
				dbservice.getUserRepository().findById(authHelper.getCurrentUser().getId()).orElseThrow().getSquad()
						.getHumans().stream().sorted(Comparator.comparing(Human::getLastname))
						.collect(Collectors.toList()));
		var stats = dbservice.getStatsRepository().findByAuthor(user.getLogin());
		model.addAttribute("reasons",
				stats.stream().map(StatsRecord::getReason)
						.filter(reason -> !SettingsRepository.getReplacements().containsKey(reason))
						.collect(Collectors.groupingBy(reason -> reason, Collectors.counting())));
		model.addAttribute("events",
				stats.stream().map(StatsRecord::getType).distinct().sorted().collect(Collectors.toList()));
		return "squadcommander/stats_overview";
	}

	@GetMapping("/stats/report")
	public String stats_forEvent(@RequestParam String event_name, Model model) {
		return statsProcessor.reportEvent_user(authHelper.getCurrentUser(), event_name, model);
	}

	@GetMapping("/stats/event_table")
	public String stats_eventTable(@RequestParam String event_name, Model model) {
		return statsProcessor.showEventTable_user(authHelper.getCurrentUser(), event_name, model);
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, @RequestParam Optional<String> eventid, Model model) {
		return statsProcessor.getEventByDateAndFilters_user(authHelper.getCurrentUser(), date, eventid,
				Optional.empty(), model);
	}

	@GetMapping("/stats/table")
	public String stats_allMarksTable(Model model) {
		return statsProcessor.showFullTable_user(authHelper.getCurrentUser(), model);
	}

	@GetMapping("/stats/personal")
	public String stats_personal(@RequestParam int id, Model model) {
		Human human = dbservice.getHumanRepository().findById(id).orElseThrow();
		return statsProcessor.getPersonReport(human, model);

	}

	@GetMapping("/stats/personal_full/table")
	public String stats_personalTable_full(@RequestParam int id, Model model) {
		Human human = dbservice.getHumanRepository().findById(id)
				.orElseThrow(() -> new NormalException("The person not found"));
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByHuman(human, Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal/table")
	public String stats_personalTable(@RequestParam int id, Model model) {
		Human human = dbservice.getHumanRepository().findById(id)
				.orElseThrow(() -> new NormalException("The person not found"));
		return statsProcessor.getTableForPerson_user(authHelper.getCurrentUser(), human, model);
	}

	@GetMapping("/stats/report_me")
	public String stats_report_me(@RequestParam boolean include_groups, Model model) {
		return statsProcessor.report_user(authHelper.getCurrentUser(), include_groups, model);
	}

	@GetMapping("/humans")
	public String humans_data(Model model) {
		model.addAttribute("humans",
				dbservice.getUserRepository().findById(authHelper.getCurrentUser().getId()).orElseThrow().getSquad()
						.getHumans().stream().sorted(Comparator.comparing(Human::getLastname))
						.collect(Collectors.toList()));
		return "public/humans_rawtable";
	}
}
