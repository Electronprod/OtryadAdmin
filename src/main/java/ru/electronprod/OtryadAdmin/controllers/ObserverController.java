package ru.electronprod.OtryadAdmin.controllers;

import java.util.Comparator;
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
import ru.electronprod.OtryadAdmin.models.Group;
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

	@GetMapping("")
	public String stats_overview(Model model) {
		model.addAttribute("name", auth.getCurrentUser().getName());
		model.addAttribute("user_role", auth.getCurrentUser().getRole());
		// Squads view
		model.addAttribute("squadList", dbservice.getSquadRepository().findAll());
		// Groups view
		model.addAttribute("groups", dbservice.getGroupRepository().findAll());
		// Other info
		model.addAttribute("people_size", dbservice.getHumanRepository().getSize());
		model.addAttribute("people_missed", dbservice.getStatsRepository().countByIsPresent(false));
		model.addAttribute("people_attended", dbservice.getStatsRepository().countByIsPresent(true));
		model.addAttribute("people_missed_today",
				dbservice.getStatsRepository().countByDateAndIsPresent(DBService.getStringDate(), false));
		model.addAttribute("people_attended_today",
				dbservice.getStatsRepository().countByDateAndIsPresent(DBService.getStringDate(), true));
		model.addAttribute("commanders_marked_today",
				dbservice.getStatsRepository().countDistinctAuthorsByDate(DBService.getStringDate()));
		model.addAttribute("events",
				dbservice.getStatsRepository().findDistinctTypes("ROLE_SQUADCOMMANDER").stream().sorted().toList());
		return "observer/stats_overview";
	}

	@GetMapping("/stats")
	public String overview() {
		// A placeholder for the future
		return "forward:/observer";
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByDate(date.replaceAll("-", "."),
				Sort.by(Sort.Direction.DESC, "id")));
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
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByType(event_name, Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String stats_personal(@RequestParam String name, Model model) {
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		statsWorker.getMainPersonalReportModel(dbservice.getStatsRepository().findByHuman(human), model, true);
		model.addAttribute("person", human.getLastname() + " " + human.getName());
		model.addAttribute("human_id", human.getId());
		return "observer/personal_stats";
	}

	@GetMapping("/stats/personal/table")
	public String stats_personalTable(@RequestParam String name, Model model) {
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/observer/stats?error_notfound";
		}
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByHuman(human, Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal_id")
	public String stats_personal(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/observer/stats?error_notfound";
		}
		statsWorker.getMainPersonalReportModel(dbservice.getStatsRepository().findByHuman(human.get()), model, true);
		model.addAttribute("person", human.get().getLastname() + " " + human.get().getName());
		model.addAttribute("human_id", human.get().getId());
		return "observer/personal_stats";
	}

	@GetMapping("/stats/personal/table_id")
	public String stats_personalTable(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanRepository().findById(id);
		if (human.isEmpty()) {
			return "redirect:/observer/stats?error_notfound";
		}
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByHuman(human.get(), Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/squad/{id}")
	public String stats_squad_overview(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("name", squad.get().getCommander().getName());
		model.addAttribute("humans",
				squad.get().getHumans().stream().sorted(Comparator.comparing(obj -> obj.getLastname())).toList());
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
				squad.get().getCommander().getLogin(), Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/squad/{id}/report")
	public String stats_squad_eventTable(@PathVariable("id") int id, @RequestParam String event_name, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		List<StatsRecord> stats = dbservice.getStatsRepository().findByTypeAndAuthor(event_name,
				squad.get().getCommander().getLogin(), Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("data", statsWorker.getEventReport(stats));
		model.addAttribute("eventName", event_name);
		return "public/event_stats";
	}

	@GetMapping("/stats/squad/{id}/table")
	public String stats_squad_allTable(@PathVariable("id") int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadRepository().findById(id);
		if (squad.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("statss", dbservice.getStatsRepository().findByAuthor(squad.get().getCommander().getLogin(),
				Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/group/{id}")
	public String stats_group_overview(@PathVariable("id") int id, Model model) {
		Optional<Group> group = dbservice.getGroupRepository().findById(id);
		if (group.isEmpty())
			return "redirect:/observer/stats?error_notfound";
		model.addAttribute("group", group.get());
		model.addAttribute("humans",
				group.get().getHumans().stream().sorted(Comparator.comparing(obj -> obj.getLastname())).toList());
		model.addAttribute("group_events",
				dbservice.getStatsRepository().findDistinctTypesGroup(group.get().getName()));
		return "observer/groupstats/stats_overview";
	}

	@GetMapping("/stats/group/date")
	public String stats_group_date(@RequestParam String date, @RequestParam String groupname, Model model) {
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByDate(date.replaceAll("-", "."), Sort.by(Sort.Direction.DESC, "id"))
						.stream().filter(s -> s.getGroup().equals(groupname)));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/group/table")
	public String stats_group_allTable(@RequestParam String groupname, Model model) {
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByGroup(groupname, Sort.by(Sort.Direction.DESC, "date")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/data")
	public String humans_data(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "public/humans_rawtable";
	}
}
