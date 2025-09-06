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
import ru.electronprod.OtryadAdmin.services.StatsHandler;
import ru.electronprod.OtryadAdmin.utils.Answer;

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
	private StatsHandler statsHelper;

	@GetMapping("")
	public String overview() {
		// A placeholder for the future
		return "forward:/squadcommander/mark";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("humanList", dbservice.getUserRepository().findById(user.getId()).orElseThrow().getSquad()
				.getHumans().stream().sorted(Comparator.comparing(Human::getLastname)).collect(Collectors.toList()));
		model.addAttribute("reasons_for_absences_map", SettingsRepository.getReasons_for_absences());
		model.addAttribute("event_types_map", SettingsRepository.getEvent_types());
		model.addAttribute("user_name", user.getName());
		return "squadcommander/mark";
	}

	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody MarkDTO dto) {
		try {
			var humans = dbservice.getSquadRepository().findByCommander(authHelper.getCurrentUser()).getHumans();
			int event_id = statsHelper.mark_group(dto, authHelper.getCurrentUser(), humans, null);
			return ResponseEntity.accepted().body(Answer.marked(event_id));
		} catch (Exception e) {
			log.error("Mark error (squadcommander.mark):", e);
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
		List<StatsRecord> stats = dbservice.getStatsRepository().findByTypeAndAuthor(event_name,
				authHelper.getCurrentUser().getLogin());
		model.addAttribute("data", statsHelper.getEventReport(stats));
		model.addAttribute("eventName", event_name);
		return "public/event_stats";
	}

	@GetMapping("/stats/event_table")
	public String stats_eventTable(@RequestParam String event_name, Model model) {
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByTypeAndAuthor(event_name, authHelper.getCurrentUser().getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, @RequestParam Optional<String> eventid, Model model) {
		try {
			var data = dbservice.getStatsRepository().findByDateAndAuthor(DBService.getStringDate(date),
					authHelper.getCurrentUser().getLogin(), Sort.by(Sort.Direction.DESC, "id"));
			if (eventid.isPresent())
				data = data.stream().filter(e -> String.valueOf(e.getEvent_id()).equals(eventid.get())).toList();
			model.addAttribute("statss", data);
		} catch (Exception e) {
		}
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/table")
	public String stats_allMarksTable(Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByAuthor(authHelper.getCurrentUser().getLogin(),
				Sort.by(Sort.Direction.DESC, "date")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String stats_personal(@RequestParam int id, Model model) {
		Human human = dbservice.getHumanRepository().findById(id).orElseThrow();
		statsHelper.getMainPersonalReportModel(
				dbservice.getStatsRepository().findByHumanAndAuthor(human, authHelper.getCurrentUser().getLogin()),
				model, false);
		model.addAttribute("person", human.getLastname() + " " + human.getName());
		return "squadcommander/personal_stats";
	}

	@GetMapping("/stats/personal_full")
	public String stats_personal_full(@RequestParam int id, Model model) {
		Human human = dbservice.getHumanRepository().findById(id).orElseThrow();
		statsHelper.getMainPersonalReportModel(dbservice.getStatsRepository().findByHuman(human), model, true);
		model.addAttribute("person", human.getLastname() + " " + human.getName());
		model.addAttribute("human_id", human.getId());
		return "observer/personal_stats";
	}

	@GetMapping("/stats/personal_full/table")
	public String stats_personalTable_full(@RequestParam int id, Model model) {
		Human human = dbservice.getHumanRepository().findById(id).orElseThrow();
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByHuman(human, Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal/table")
	public String stats_personalTable(@RequestParam int id, Model model) {
		Human human = dbservice.getHumanRepository().findById(id).orElseThrow();
		model.addAttribute("statss", dbservice.getStatsRepository().findByHumanAndAuthor(human,
				authHelper.getCurrentUser().getLogin(), Sort.by(Sort.Direction.DESC, "date")));
		return "public/statsview_rawtable";
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
