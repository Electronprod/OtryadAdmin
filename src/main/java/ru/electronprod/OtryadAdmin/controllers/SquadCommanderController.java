package ru.electronprod.OtryadAdmin.controllers;

import java.util.*;

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
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsService;
import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.*;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.ReportService;

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
	private ReportService statsHelper;

	@GetMapping("")
	public String overview() {
		// A placeholder for the future
		return "forward:/squadcommander/mark";
	}

	@GetMapping("/humans")
	public String humans(Model model) {
		model.addAttribute("user_role", authHelper.getCurrentUser().getRole());
		model.addAttribute("humans", dbservice.getUserService().findById(authHelper.getCurrentUser().getId())
				.orElseThrow().getSquad().getHumans());
		return "public/humans_rawtable";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("humanList",
				dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad().getHumans());
		model.addAttribute("reasons_for_absences_map", SettingsService.getReasons_for_absences());
		model.addAttribute("event_types_map", SettingsService.getEvent_types());
		model.addAttribute("login", user.getLogin());
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

	@GetMapping("/stats/date")
	public String statsTable_date(@RequestParam String date, Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<SquadStats> statsList = dbservice.getStatsService().findByDate(date.replaceAll("-", "."));
		statsList.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model.addAttribute("statss", statsList);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/table")
	public String deleteStats(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("statss", dbservice.getStatsService().findByAuthor(user.getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/report")
	public String general_stats(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("dataMap",
				statsHelper.squad_generateGlobalReport(dbservice.getStatsService().findByAuthor(user.getLogin())));
		return "squadcommander/general_stats";
	}

	@GetMapping("/stats/personal/table")
	public String personal_statsTable(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		List<SquadStats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		statsList.removeIf(stats -> stats.getHuman().getId() != id);
		model.addAttribute("statss", statsList);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String personal_stats(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		// Getting stats for user's humans
		Human human = dbservice.getHumanService().findById(id).orElse(new Human());
		List<SquadStats> s = human.getStats();
		s.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model = statsHelper.squad_generatePersonalReport(s, model);
		model.addAttribute("person", human.getName() + " " + human.getLastname());
		return "squadcommander/personal_stats";
	}

	@GetMapping("/stats")
	public String stats_overview(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("humans",
				dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad().getHumans());
		return "squadcommander/stats_overview";
	}
}
