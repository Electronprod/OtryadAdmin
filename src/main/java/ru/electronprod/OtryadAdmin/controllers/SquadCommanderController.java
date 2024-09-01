package ru.electronprod.OtryadAdmin.controllers;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.filesystem.LanguageService;
import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;
import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.*;
import ru.electronprod.OtryadAdmin.models.helpers.StatsFormHelper;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.StatsHelperService;

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
	private StatsHelperService statsHelper;

	@GetMapping("")
	public String overview(Model model) {
		User user = authHelper.getCurrentUser();
		Squad squad = dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad();
		// Session data
		model.addAttribute("squadname", squad.getSquadName());
		model.addAttribute("login", user.getLogin());
		model.addAttribute("commander", squad.getCommanderName());
		model.addAttribute("userid", user.getId());
		model.addAttribute("squadid", squad.getId());
		// News
		model.addAttribute("newsList", dbservice.getNewsService().getLast5());
		return "squadcommander/overview.html";
	}

	@GetMapping("/humans")
	public String humans(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("humans",
				dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad().getHumans());
		return "public/humans_rawtable.html";
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
		return "squadcommander/mark.html";
	}

	@PostMapping("/mark")
	public String markAbsent(@ModelAttribute StatsFormHelper detail, @RequestParam("statsType") String statsType) {
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
			StatsFormHelper s = new StatsFormHelper();
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
		// Formatting date from 2024-07-24 to 24.07.2024
//		try {
//			String[] pieces = date.split("-");
//			date = pieces[2] + "." + pieces[1] + "." + pieces[0];
//		} catch (Exception e) {
//			System.err.println("[/squadcommander/stats/date] date warn: " + e.getMessage());
//			return "redirect:/squadcommander?server_incorrectreq";
//		}
		List<Stats> statsList = dbservice.getStatsService().findByDate(date.replaceAll("-", "."));
		statsList.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model.addAttribute("statss", statsList);
		return "/public/statsview_rawtable.html";
	}

	@GetMapping("/stats/table")
	public String deleteStats(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("statss", dbservice.getStatsService().findByAuthor(user.getLogin()));
		return "/public/statsview_rawtable.html";
	}

	@GetMapping("/stats/report")
	public String general_stats(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("dataMap",
				statsHelper.squad_generateGlobalReport(dbservice.getStatsService().findByAuthor(user.getLogin())));
		return "/squadcommander/general_stats.html";
	}

	@GetMapping("/stats/personal/table")
	public String personal_statsTable(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Stats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		statsList.removeIf(stats -> stats.getHuman().getId() != id);
		model.addAttribute("statss", statsList);
		return "/public/statsview_rawtable.html";
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
		return "/squadcommander/personal_stats.html";
	}

	@GetMapping("/stats/personal_old")
	public String personal_stats_old(@RequestParam int id, Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		// Getting stats for user's humans
		Human human = dbservice.getHumanService().findById(id).orElse(new Human());
		List<Stats> s = dbservice.getStatsService().findByHuman(human);
		s.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model = statsHelper.old_squad_generatePersonalReport(model, s);
		model.addAttribute("name", human.getName() + " " + human.getLastname());
		return "/squadcommander/personal_stats_old.html";
	}

	@GetMapping("/stats")
	public String stats_overview(Model model) {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("humans",
				dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad().getHumans());
		return "/squadcommander/stats_overview.html";
	}
}
