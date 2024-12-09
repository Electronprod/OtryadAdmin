package ru.electronprod.OtryadAdmin.controllers;

import java.util.*;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.models.*;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.services.StatsWorker;

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
	private StatsWorker statsHelper;

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
		model.addAttribute("login", user.getName());
		return "squadcommander/mark";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody Map<String, Object> requestBody) {
		User user = authHelper.getCurrentUser();
		List<?> uncheckedPeopleList = (List<?>) requestBody.get("uncheckedPeople");
		JSONArray uncheckedPeopleArray = new JSONArray();
		uncheckedPeopleArray.addAll(uncheckedPeopleList);
		JSONObject answer = new JSONObject();
		try {
			statsHelper.squad_mark(uncheckedPeopleArray, String.valueOf(requestBody.get("statsType")), user);
			answer.put("result", "success");
			return ResponseEntity.ok(answer.toJSONString());
		} catch (ParseException e) {
			log.warn("Error parsing input JSON for " + user.getLogin() + ". JSONArray: "
					+ uncheckedPeopleArray.toJSONString());
			answer.put("result", "error");
			answer.put("message", "Error parsing input JSON.");
			answer.put("user", user.getLogin());
			answer.put("JSONArray", uncheckedPeopleArray.toJSONString());
			return ResponseEntity.internalServerError().body(answer.toJSONString());
		}
	}

	@GetMapping("/stats")
	public String stats_overview(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("humans",
				dbservice.getUserRepository().findById(user.getId()).orElseThrow().getSquad().getHumans());
		model.addAttribute("events", dbservice.getStatsRepository().findByAuthor(user.getLogin()).stream()
				.map(StatsRecord::getType).distinct().sorted().collect(Collectors.toList()));
		return "squadcommander/stats_overview";
	}

	@GetMapping("/stats/report")
	public String stats_forEvent(@RequestParam String event_name, Model model) {
		User user = authHelper.getCurrentUser();
		List<StatsRecord> stats = dbservice.getStatsRepository().findByTypeAndAuthor(event_name, user.getLogin());
		model.addAttribute("data", statsHelper.getEventReport(stats));
		model.addAttribute("eventName", event_name);
		return "public/event_stats";
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByDateAndAuthor(date.replaceAll("-", "."), user.getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/table")
	public String stats_allMarksTable(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("statss", dbservice.getStatsRepository().findByAuthor(user.getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String stats_personal(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		Human human = dbservice.getHumanRepository().findById(id).orElseThrow();
		statsHelper.getMainPersonalReportModel(
				dbservice.getStatsRepository().findByHumanAndAuthor(human, user.getLogin()), model);
		model.addAttribute("person", human.getLastname() + " " + human.getName());
		return "squadcommander/personal_stats";
	}

	@GetMapping("/stats/personal/table")
	public String stats_personalTable(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		Human human = dbservice.getHumanRepository().findById(id).orElseThrow();
		model.addAttribute("statss", dbservice.getStatsRepository().findByHumanAndAuthor(human, user.getLogin()));
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
