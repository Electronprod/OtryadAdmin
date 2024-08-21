package ru.electronprod.OtryadAdmin.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.News;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.Stats;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.services.StatsHelperService;
import ru.electronprod.OtryadAdmin.services.data.DBService;

@Controller
@RequestMapping("/observer")
@PreAuthorize("hasAuthority('ROLE_OBSERVER') or hasAuthority('ROLE_ADMIN')")
public class ObserverController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private StatsHelperService statsHelper;

	/*
	 * Overview page
	 */
	@GetMapping("")
	public String overview(Model model) {
		model.addAttribute("login", dbservice.getAuthService().getCurrentUser().getLogin());
		model.addAttribute("newsList", dbservice.getNewsService().getLast5());
		return "/observer/overview.html";
	}

	/*
	 * News creator form
	 */
	@GetMapping("/addnews")
	public String addNews(Model model) {
		model.addAttribute("news", new News());
		return "/observer/add_news.html";
	}

	@PostMapping("/addnews")
	public String addNews(@ModelAttribute("user") News news) {
		news.setAuthor(dbservice.getAuthService().getCurrentUser().getLogin());
		dbservice.getNewsService().createNews(news);
		return "redirect:/observer?published";
	}

	/*
	 * Statistics pages
	 */
	@GetMapping("/stats")
	public String stats_overview(Model model) {
		// Squads view
		model.addAttribute("squadList", dbservice.getSquadService().findAll());
		// People view
		model.addAttribute("people_size", dbservice.getHumanService().getSize());
		model.addAttribute("people_missed", dbservice.getStatsService().getRepository().countByIsPresent(false));
		model.addAttribute("people_attended", dbservice.getStatsService().getRepository().countByIsPresent(true));
		return "/observer/stats_overview.html";
	}

	@GetMapping("/stats/squad/{id}")
	public String stats(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadService().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("humans", squad.get().getHumans());
		return "/observer/squadstats/stats_overview.html";
	}

	@GetMapping("/stats/squad/{id}/report")
	public String statsReport(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadService().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model = statsHelper.generateGeneralReport(model,
				dbservice.getStatsService().findByAuthor(squad.get().getCommander().getLogin()));
		return "/observer/squadstats/general_stats.html";
	}

	@GetMapping("/stats/squad/{id}/table")
	public String statsTable(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadService().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("statss", dbservice.getStatsService().findByAuthor(squad.get().getCommander().getLogin()));
		return "/public/statsview_rawtable.html";
	}

	@GetMapping("/stats/personal")
	public String personalStats(@RequestParam String name, Model model) {
		Human human = statsHelper.findMostSimilarHuman(name, dbservice.getHumanService().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		model = statsHelper.generatePersonalReport(model, human.getStats());
		model.addAttribute("name", human.getName() + " " + human.getLastname());
		return "/observer/personal_stats.html";
	}

	@GetMapping("/data")
	public String getAllData(Model model) {
		model.addAttribute("humans", dbservice.getHumanService().findAll());
		return "public/humans_rawtable.html";
	}
}
