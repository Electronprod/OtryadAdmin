package ru.electronprod.OtryadAdmin.controllers;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;
import ru.electronprod.OtryadAdmin.data.filesystem.AppLanguageRepository;
import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.*;
import ru.electronprod.OtryadAdmin.models.helpers.SquadMarksDataModel;
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
	private OptionService optionService;
	@Autowired
	private ReportService statsHelper;

	@GetMapping("")
	public String overview() {
		// A placeholder for the future
		return "forward:/squadcommander/mark";
	}

	@GetMapping("/humans")
	public String humans(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("humans",
				dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad().getHumans());
		return "public/humans_rawtable";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("humanList",
				dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad().getHumans());
		model.addAttribute("reasons_for_absences_map", optionService.getReasons_for_absences());
		model.addAttribute("event_types_map", optionService.getEvent_types());
		model.addAttribute("login", user.getLogin());
		return "squadcommander/mark";
	}

	@PostMapping("/mark")
	public String markAbsent(@ModelAttribute SquadMarksDataModel detail, @RequestParam("statsType") String statsType) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		try {
			statsHelper.squad_mark(detail, statsType, user);
		} catch (Exception e) {
			log.error("Error marking: " + e.getMessage());
			return "redirect:/squadcommander/mark?error_unknown";
		}
		return "redirect:/squadcommander/mark?sent";
	}

	@GetMapping("/mark_allhere")
	public String markAbsent(@RequestParam("statsType") String statsType) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		try {
			SquadMarksDataModel s = new SquadMarksDataModel();
			s.setDetails(new HashMap<Integer, String>());
			statsHelper.squad_mark(s, statsType, user);
		} catch (Exception e) {
			log.error("Error marking: " + e.getMessage());
			return "redirect:/squadcommander/mark?error_unknown";
		}
		return "redirect:/squadcommander/mark?sent";
	}

	@GetMapping("/stats/date")
	public String statsTable_date(@RequestParam String date, Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Stats> statsList = dbservice.getStatsService().findByDate(date.replaceAll("-", "."));
		statsList.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model.addAttribute("statss", statsList);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/table")
	public String deleteStats(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("statss", dbservice.getStatsService().findByAuthor(user.getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/report")
	public String general_stats(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("dataMap",
				statsHelper.squad_generateGlobalReport(dbservice.getStatsService().findByAuthor(user.getLogin())));
		return "squadcommander/general_stats";
	}

	@GetMapping("/stats/personal/table")
	public String personal_statsTable(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Stats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		statsList.removeIf(stats -> stats.getHuman().getId() != id);
		model.addAttribute("statss", statsList);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String personal_stats(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		// Getting stats for user's humans
		Human human = dbservice.getHumanService().findById(id).orElse(new Human());
		List<Stats> s = human.getStats();
		s.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model = statsHelper.squad_generatePersonalReport(s, model);
		model.addAttribute("person", human.getName() + " " + human.getLastname());
		return "squadcommander/personal_stats";
	}

	@GetMapping("/stats")
	public String stats_overview(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("humans",
				dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad().getHumans());
		return "squadcommander/stats_overview";
	}
}
