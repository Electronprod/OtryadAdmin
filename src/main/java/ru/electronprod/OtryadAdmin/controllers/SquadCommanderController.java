package ru.electronprod.OtryadAdmin.controllers;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ru.electronprod.OtryadAdmin.models.*;
import ru.electronprod.OtryadAdmin.models.helpers.StatsFormHelper;
import ru.electronprod.OtryadAdmin.services.data.DBService;

@Controller
@RequestMapping("/squadcommander")
@PreAuthorize("hasAuthority('ROLE_SQUADCOMMANDER')")
public class SquadCommanderController {
	@Autowired
	private DBService dbservice;

	@GetMapping("")
	public String overview(Model model) {
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
		return "squadcommander/humans.html";
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

	@GetMapping("/mark/del")
	@Deprecated
	public String deleteStats(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Stats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		model.addAttribute("statss", statsList);
		return "/squadcommander/mark_delete.html";
	}

	@GetMapping("/stats")
	public String stats(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		// Getting stats for user's humans
		List<Stats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		String[] types = { "general", "duty", "walk", "other1", "other2" };
		for (String type : types) {
			List<Stats> typedStats = new ArrayList<Stats>();
			typedStats.addAll(statsList);
			// Deleting other types from list
			typedStats.removeIf(stats -> !stats.getType().equals(type));
			Map<Human, Integer> map = new HashMap<Human, Integer>();
			// For each Stats object
			for (Stats stats : typedStats) {
				Human human = stats.getHuman();
				if (stats.isPresent()) {
					// Человек пришел
					// Adding to map
					if (map.containsKey(human)) {
						map.put(human, map.get(human) + 1);
					} else {
						map.put(human, 1);
					}
				} else {
					// Человек не пришел
					if (!map.containsKey(human)) {
						map.put(human, 0);
					}
				}
			}
			// Sorting
			Map<Human, Integer> sortedMap = map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			model.addAttribute(type + "Entry", sortedMap);
			typedStats = null;
		}
		return "/squadcommander/stats.html";
	}

	@GetMapping("/personal_stats")
	public String personal_stats(Model model) {
		return "/squadcommander/personal_stats.html";
	}

}
