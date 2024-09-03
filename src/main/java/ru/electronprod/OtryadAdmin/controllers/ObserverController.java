package ru.electronprod.OtryadAdmin.controllers;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.News;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.Stats;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.SearchService;
import ru.electronprod.OtryadAdmin.services.StatsHelperService;

@Controller
@RequestMapping("/observer")
@PreAuthorize("hasAuthority('ROLE_OBSERVER') or hasAuthority('ROLE_ADMIN')")
public class ObserverController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;
	@Autowired
	private StatsHelperService statsHelper;

	/*
	 * Overview page
	 */
	@GetMapping("")
	public String overview(Model model) {
		model.addAttribute("login", authHelper.getCurrentUser().getLogin());
		model.addAttribute("newsList", dbservice.getNewsService().getLast5());
		return "observer/overview";
	}

	/*
	 * News creator form
	 */
	@GetMapping("/addnews")
	public String addNews(Model model) {
		model.addAttribute("news", new News());
		return "observer/add_news";
	}

	@PostMapping("/addnews")
	public String addNews(@ModelAttribute("user") News news) {
		news.setAuthor(authHelper.getCurrentUser().getLogin());
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
		model.addAttribute("people_missed", dbservice.getStatsService().countByIsPresent(false));
		model.addAttribute("people_attended", dbservice.getStatsService().countByIsPresent(true));
		return "observer/stats_overview";
	}

	@GetMapping("/stats/squad/{id}")
	public String stats(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadService().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("humans", squad.get().getHumans());
		return "observer/squadstats/stats_overview";
	}

	@GetMapping("/stats/squad/{id}/report")
	public String SquadStatsReport(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadService().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("dataMap", statsHelper.squad_generateGlobalReport(
				dbservice.getStatsService().findByAuthor(squad.get().getCommander().getLogin())));
		return "observer/squadstats/general_stats";
	}

	@GetMapping("/stats/squad/{id}/table")
	public String statsTable(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadService().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("statss", dbservice.getStatsService().findByAuthor(squad.get().getCommander().getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String personalStats(@RequestParam String name, Model model) {
		Human human = SearchService.findMostSimilarHuman(name, dbservice.getHumanService().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		List<Stats> s = dbservice.getStatsService().findByHuman(human);
		model = statsHelper.squad_generatePersonalReport(s, model);
		model.addAttribute("person", human.getName() + " " + human.getLastname());
		return "observer/personal_stats";
	}

	@GetMapping("/stats/personal/table")
	public String personalStatsTable(@RequestParam String name, Model model) {
		Human human = SearchService.findMostSimilarHuman(name, dbservice.getHumanService().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		List<Stats> s = dbservice.getStatsService().findByHuman(human);
		model.addAttribute("statss", s);
		return "public/statsview_rawtable";
	}

	@GetMapping("/data")
	public String getAllData(Model model) {
		model.addAttribute("humans", dbservice.getHumanService().findAll());
		return "public/humans_rawtable";
	}
}
