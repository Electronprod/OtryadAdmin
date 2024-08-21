package ru.electronprod.OtryadAdmin.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import ru.electronprod.OtryadAdmin.models.*;
import ru.electronprod.OtryadAdmin.models.helpers.StatsFormHelper;
import ru.electronprod.OtryadAdmin.services.StatsHelperService;
import ru.electronprod.OtryadAdmin.services.data.DBService;

@Controller
@RequestMapping("/squadcommander")
@PreAuthorize("hasAuthority('ROLE_SQUADCOMMANDER')")
public class SquadCommanderController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private StatsHelperService statsHelper;

	@GetMapping("")
	public String overview(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		Squad squad = dbservice.getAuthService().findById(user.getId()).get().getSquad();
		// Session data
		model.addAttribute("squadname", squad.getSquadName());
		model.addAttribute("login", user.getLogin());
		model.addAttribute("commander", squad.getCommanderName());
		model.addAttribute("userid", user.getId());
		model.addAttribute("squadid", squad.getId());
		// News
		List<News> news = dbservice.getNewsService().getLast5();
		model.addAttribute("newsList", news);
		return "squadcommander/overview.html";
	}

	@GetMapping("/humans")
	public String humans(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Human> humans = dbservice.getAuthService().findById(user.getId()).orElseThrow().getSquad().getHumans();
		model.addAttribute("humans", humans);
		return "public/humans_rawtable.html";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		// Getting humans from database using user's ID
		List<Human> humans = dbservice.getAuthService().findById(user.getId()).orElseThrow().getSquad().getHumans();
		model.addAttribute("humanList", humans);
		return "squadcommander/mark.html";
	}

	@PostMapping("/mark")
	public String markAbsent(@ModelAttribute StatsFormHelper detail, @RequestParam("statsType") String statsType) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		// Getting all required data
		Map<Integer, String> details1 = detail.getDetails(); // human ID + Reason
		List<Human> remainingHumans = dbservice.getAuthService().findById(user.getId()).orElseThrow().getSquad()
				.getHumans();
		int event_id = dbservice.getStatsService().findMaxEventIDValue() + 1;
		// Generating Stats array
		List<Stats> statsArr = new ArrayList<Stats>();
		// Those who didn't come
		details1.forEach((id, reason) -> {
			// System.out.println("ID: " + id + ", Reason: " + reason);
			Human human1 = remainingHumans.stream().filter(human -> human.getId() == id).findFirst().orElseThrow();
			Stats stats = new Stats(human1);
			stats.setAuthor(user.getLogin());
			stats.setDate(dbservice.getStringDate());
			stats.setPresent(false);
			stats.setReason(reason);
			stats.setType(statsType);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			statsArr.add(stats);
			remainingHumans.remove(human1);
		});
		// Those who come
		for (Human human : remainingHumans) {
			Stats stats = new Stats(human);
			stats.setAuthor(user.getLogin());
			stats.setDate(dbservice.getStringDate());
			stats.setPresent(true);
			stats.setReason("error:present");
			stats.setType(statsType);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			statsArr.add(stats);
		}
		// Saving result to database
		dbservice.getStatsService().getRepository().saveAll(statsArr);
		return "redirect:/squadcommander/mark?sent";
	}

	@GetMapping("/mark_allhere")
	public String markAbsent(@RequestParam("statsType") String statsType) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		// Getting all required data
		List<Human> allHumans = dbservice.getAuthService().findById(user.getId()).orElseThrow().getSquad().getHumans();
		int event_id = dbservice.getStatsService().findMaxEventIDValue() + 1;
		// Generating Stats array
		List<Stats> statsArr = new ArrayList<Stats>();
		for (Human human : allHumans) {
			Stats stats = new Stats(human);
			stats.setAuthor(user.getLogin());
			stats.setDate(dbservice.getStringDate());
			stats.setPresent(true);
			stats.setReason("error:present");
			stats.setType(statsType);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			statsArr.add(stats);
		}
		// Saving result to database
		dbservice.getStatsService().getRepository().saveAll(statsArr);
		return "redirect:/squadcommander/mark?sent";
	}

	@GetMapping("/stats/date")
	public String statsTable_date(@RequestParam String date, Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		// Formatting date from 2024-07-24 to 24.07.2024
		try {
			String[] pieces = date.split("-");
			date = pieces[2] + "." + pieces[1] + "." + pieces[0];
		} catch (Exception e) {
			System.err.println("[/squadcommander/stats/date] date warn: " + e.getMessage());
			return "redirect:/squadcommander?server_incorrectreq";
		}
		List<Stats> statsList = dbservice.getStatsService().getRepository().findByDate(date);
		statsList.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model.addAttribute("statss", statsList);
		return "/public/statsview_rawtable.html";
	}

	@GetMapping("/stats/table")
	public String deleteStats(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("statss", dbservice.getStatsService().findByAuthor(user.getLogin()));
		return "/public/statsview_rawtable.html";
	}

	@GetMapping("/stats/report")
	public String statsReport(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model = statsHelper.generateGeneralReport(model, dbservice.getStatsService().findByAuthor(user.getLogin()));
		return "/squadcommander/general_stats.html";
	}

	@GetMapping("/stats/personal/table")
	public String personal_statsTable(@RequestParam int id, Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Stats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		statsList.removeIf(stats -> stats.getHuman().getId() != id);
		model.addAttribute("statss", statsList);
		return "/public/statsview_rawtable.html";
	}

	@GetMapping("/stats/personal")
	public String personal_stats(@RequestParam int id, Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		// Getting stats for user's humans
		List<Stats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		statsList.removeIf(stats -> (stats.getHuman().getId() != id));
		model = statsHelper.generatePersonalReport(model, statsList);
		Human human = dbservice.getHumanService().findById(id).orElse(new Human());
		model.addAttribute("name", human.getName() + " " + human.getLastname());
		return "/squadcommander/personal_stats.html";
	}

	@GetMapping("/stats")
	public String stats(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		model.addAttribute("humans",
				dbservice.getAuthService().findById(user.getId()).orElseThrow().getSquad().getHumans());
		return "/squadcommander/stats_overview.html";
	}
}
