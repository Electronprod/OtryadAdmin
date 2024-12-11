package ru.electronprod.OtryadAdmin.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
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
		model.addAttribute("login", auth.getCurrentUser().getName());
		model.addAttribute("user_role", auth.getCurrentUser().getRole());
		// Squads view
		model.addAttribute("squadList", dbservice.getSquadRepository().findAll());
		// People view
		model.addAttribute("people_size", dbservice.getHumanRepository().getSize());
		model.addAttribute("people_missed", dbservice.getStatsRepository().countByIsPresent(false));
		model.addAttribute("people_attended", dbservice.getStatsRepository().countByIsPresent(true));
		model.addAttribute("people_missed_today",
				dbservice.getStatsRepository().countByDateAndIsPresent(DBService.getStringDate(), false));
		model.addAttribute("people_attended_today",
				dbservice.getStatsRepository().countByDateAndIsPresent(DBService.getStringDate(), true));
		model.addAttribute("commanders_marked_today",
				dbservice.getStatsRepository().countDistinctAuthorsByDate(DBService.getStringDate()));
		model.addAttribute("events", dbservice.getStatsRepository().findDistinctTypes().stream().sorted().toList());
		return "observer/stats_overview";
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByDate(date.replaceAll("-", ".")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/table")
	public String stats_allTable(Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findAll(Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/report")
	public String stats_event(@RequestParam String event_name, Model model) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByType(event_name);
		model.addAttribute("data", statsWorker.getEventReport(stats));
		model.addAttribute("eventName", event_name);
		return "public/event_stats";
	}

	@GetMapping("/stats/event_table")
	public String stats_eventTable(@RequestParam String event_name, Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByType(event_name));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String stats_personal(@RequestParam String name, Model model) {
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		statsWorker.getMainPersonalReportModel(dbservice.getStatsRepository().findByHuman(human), model);
		model.addAttribute("person", human.getLastname() + " " + human.getName());
		return "observer/personal_stats";
	}

	@GetMapping("/stats/personal/table")
	public String stats_personalTable(@RequestParam String name, Model model) {
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		model.addAttribute("statss", dbservice.getStatsRepository().findByHuman(human));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal_id")
	public String stats_personal(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/observer/stats?error_notfound";
		}
		statsWorker.getMainPersonalReportModel(dbservice.getStatsRepository().findByHuman(human.get()), model);
		model.addAttribute("person", human.get().getLastname() + " " + human.get().getName());
		return "observer/personal_stats";
	}

	@GetMapping("/stats/personal/table_id")
	public String stats_personalTable(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/observer/stats?error_notfound";
		}
		model.addAttribute("statss", dbservice.getStatsRepository().findByHuman(human.get()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/squad/{id}")
	public String stats_squad_overview(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("commander", squad.get().getCommander().getName());
		model.addAttribute("humans", squad.get().getHumans());
		model.addAttribute("events", dbservice.getStatsRepository().findByAuthor(squad.get().getCommander().getLogin())
				.stream().map(StatsRecord::getType).distinct().sorted().collect(Collectors.toList()));
		return "observer/squadstats/stats_overview";
	}

	@GetMapping("/stats/squad/{id}/date")
	public String stats_squad_byDate(@PathVariable("id") int id, @RequestParam String date, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("statss", dbservice.getStatsRepository().findByDateAndAuthor(date.replaceAll("-", "."),
				squad.get().getCommander().getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/squad/{id}/report")
	public String stats_squad_eventTable(@PathVariable("id") int id, @RequestParam String event_name, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		List<StatsRecord> stats = dbservice.getStatsRepository().findByTypeAndAuthor(event_name,
				squad.get().getCommander().getLogin());
		model.addAttribute("data", statsWorker.getEventReport(stats));
		model.addAttribute("eventName", event_name);
		return "public/event_stats";
	}

	@GetMapping("/stats/squad/{id}/table")
	public String stats_squad_allTable(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByAuthor(squad.get().getCommander().getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/data")
	public String humans_data(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "public/humans_rawtable";
	}
}
