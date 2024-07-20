package ru.electronprod.OtryadAdmin.controllers;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ru.electronprod.OtryadAdmin.data.UsrDetails;
import ru.electronprod.OtryadAdmin.models.*;
import ru.electronprod.OtryadAdmin.models.helpers.HumanHelper;
import ru.electronprod.OtryadAdmin.models.helpers.StatsHelper;
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
		List<Human> humans = dbservice.getAuthService().findById(user.getId()).orElseThrow().getSquad().getHumans();
		List<HumanHelper> humanHelpers = new ArrayList<HumanHelper>();
		for (Human human : humans)
			humanHelpers.add(HumanHelper.fillDefaultValues(human));
		model.addAttribute("humanList", humanHelpers);
		return "squadcommander/mark.html";
	}

	@PostMapping("/mark")
	public String markForm(@RequestParam List<Integer> selectedIds, @RequestParam("statsType") String statsType,
			Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Human> humans = dbservice.getHumanService().findByIds(selectedIds);
		for (Human human : humans) {
			List<Stats> statsList = human.getStats();
			Stats stats = new Stats(human);
			stats.setAuthor(user.getLogin());
			stats.setUser_role(user.getRole());
			stats.setType(statsType);
			stats.setDate(dbservice.getStringDate());
			human.getStats().add(stats);
			dbservice.getStatsService().save(stats);
		}
		return "redirect:/squadcommander?marked";
	}

	@GetMapping("/mark/del")
	public String deleteStats(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Stats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		model.addAttribute("statss", statsList);
		return "/squadcommander/mark_delete.html";
	}

	@GetMapping("/mark/delete")
	public String deleteStatsAction(@RequestParam() int id) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		Optional<Stats> stats = dbservice.getStatsService().findById(id);
		if (stats.isEmpty() || stats.get().getAuthor().equals(user.getLogin()) == false) {
			return "redirect:/squadcommander?error_notfound";
		}
		dbservice.getStatsService().deleteById(id);
		return "redirect:/squadcommander?deleted";
	}

	@GetMapping("/stats")
	public String stats(Model model) {
		User user = dbservice.getAuthService().getCurrentUser();
		if (user == null)
			return "redirect:/squadcommander?error_usernotfound";
		List<Stats> statsList = dbservice.getStatsService().findByAuthor(user.getLogin());
		String[] types = { "fee", "duty", "walk", "other1", "other2" };
		List<Human> humanList = dbservice.getAuthService().findById(user.getId()).get().getSquad().getHumans();
		// For each type
		for (String type : types) {
			List<Stats> typedStats = new ArrayList();
			typedStats.addAll(statsList);
			// Deleting other types from list
			typedStats.removeIf(stats -> !stats.getType().equals(type));
			Map<Human, Integer> map = new HashMap();
			// For each Stats object
			for (Stats stats : typedStats) {
				Human human = stats.getHuman();
				// Adding to map
				if (map.containsKey(human)) {
					map.put(human, map.get(human) + 1);
				} else {
					map.put(human, 1);
				}
			}
			// Если кто-то не посещал
			List<Human> other = new ArrayList<Human>();
			other.addAll(humanList);
			// Удаляем всех, у кого было найдено Stats
			other.removeAll(map.keySet());
			if (other.isEmpty() == false) {
				for (Human human : other) {
					map.put(human, 0);
				}
			}
			other = null;
			// Sorting
			Map<Human, Integer> sortedMap = map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			// Adding to page
			model.addAttribute(type + "Entry", sortedMap);
			typedStats = null;
			sortedMap = null;
			map = null;
		}
		return "/squadcommander/stats.html";
	}
}
