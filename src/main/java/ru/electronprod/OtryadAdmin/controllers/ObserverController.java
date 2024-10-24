package ru.electronprod.OtryadAdmin.controllers;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.SquadStats;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.StatsWorker;
import ru.electronprod.OtryadAdmin.utils.SearchUtil;

@Controller
@RequestMapping("/observer")
@PreAuthorize("hasAuthority('ROLE_OBSERVER') or hasAuthority('ROLE_ADMIN')")
public class ObserverController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper auth;
	@Autowired
	private StatsWorker statsWorker;

	/*
	 * Overview page
	 */
	@GetMapping("")
	public String overview(Model model) {
		// A placeholder for the future
		return "forward:/observer/stats";
	}

	/*
	 * Statistics pages
	 */
	@GetMapping("/stats")
	public String stats_overview(Model model) {
		model.addAttribute("login", auth.getCurrentUser().getLogin());
		model.addAttribute("user_role", auth.getCurrentUser().getRole());
		// Squads view
		model.addAttribute("squadList", dbservice.getSquadRepository().findAll());
		// People view
		model.addAttribute("people_size", dbservice.getHumanRepository().getSize());
		model.addAttribute("people_missed", dbservice.getStatsRepository().countByIsPresent(false));
		model.addAttribute("people_attended", dbservice.getStatsRepository().countByIsPresent(true));
		model.addAttribute("people_missed_today", dbservice.getStatsRepository().findByDate(DBService.getStringDate())
				.stream().filter(stats -> !stats.isPresent()).count());
		model.addAttribute("people_attended_today", dbservice.getStatsRepository().findByDate(DBService.getStringDate())
				.stream().filter(stats -> stats.isPresent()).count());
		model.addAttribute("commanders_marked_today", dbservice.getStatsRepository()
				.findByDate(DBService.getStringDate()).stream().map(SquadStats::getAuthor).distinct().count());
		return "observer/stats_overview";
	}

	@GetMapping("/stats/date")
	public String statsTable_date(@RequestParam String date, Model model) {
		List<SquadStats> statsList = dbservice.getStatsRepository().findByDate(date.replaceAll("-", "."));
		model.addAttribute("statss", statsList);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/squad/{id}")
	public String stats(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("humans", squad.get().getHumans());
		return "observer/squadstats/stats_overview";
	}

	@GetMapping("/stats/squad/{id}/date")
	public String statsTable_date_squad(@PathVariable("id") int id, @RequestParam String date, Model model) {
		List<SquadStats> statsList = dbservice.getStatsRepository().findByDate(date.replaceAll("-", "."));
		statsList.removeIf(stats -> !stats.getAuthor()
				.equals(dbservice.getSquadRepository().findById(id).orElseThrow().getCommander().getLogin()));
		model.addAttribute("statss", statsList);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/squad/{id}/report")
	public String SquadStatsReport(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("dataMap", statsWorker.squad_getEventsReport(
				dbservice.getStatsRepository().findByAuthor(squad.get().getCommander().getLogin())));
		return "observer/squadstats/general_stats";
	}

	@GetMapping("/stats/squad/{id}/table")
	public String statsTable(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByAuthor(squad.get().getCommander().getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/table")
	public String statsTableAll(Model model) {
		List<SquadStats> squads = dbservice.getStatsRepository().findAll();
		model.addAttribute("statss", squads);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String personalStats(@RequestParam String name, Model model) {
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		List<SquadStats> statsArray = dbservice.getStatsRepository().findByHuman(human);
		statsWorker.getMainPersonalReportModel(statsArray, model);
		model.addAttribute("person", human.getName() + " " + human.getLastname());
		return "observer/personal_stats";
	}

	@GetMapping("/stats/personal/table")
	public String personalStatsTable(@RequestParam String name, Model model) {
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		List<SquadStats> s = dbservice.getStatsRepository().findByHuman(human);
		model.addAttribute("statss", s);
		return "public/statsview_rawtable";
	}

	@GetMapping("/data")
	public String getAllData(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll());
		return "public/humans_rawtable";
	}
}
