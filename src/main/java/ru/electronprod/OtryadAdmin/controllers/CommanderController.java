package ru.electronprod.OtryadAdmin.controllers;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.SquadStats;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.StatsWorker;
import ru.electronprod.OtryadAdmin.utils.SearchUtil;

@Slf4j
@Controller
@RequestMapping("/commander")
@PreAuthorize("hasAuthority('ROLE_COMMANDER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OBSERVER')")
public class CommanderController {
	@Autowired
	private StatsWorker statsWorker;
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;

	@GetMapping("")
	public String overview() {
		// A placeholder for the future
		return "forward:/commander/mark";
	}

	@GetMapping("/stats")
	public String stats_overview() {
		return "commander/stats_overview";
	}

	@GetMapping("/humans")
	public String getAllData(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "public/humans_rawtable";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("humanList",
				dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		model.addAttribute("login", user.getLogin());
		return "commander/mark";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody Map<String, Object> requestBody) {
		User user = authHelper.getCurrentUser();
		List<?> uncheckedPeopleList = (List<?>) requestBody.get("checkedPeople");
		JSONArray checkedPeopleArray = new JSONArray();
		checkedPeopleArray.addAll(uncheckedPeopleList);
		JSONObject answer = new JSONObject();
		int id = statsWorker.commander_mark(checkedPeopleArray, String.valueOf(requestBody.get("eventName")),
				String.valueOf(requestBody.get("date")), user);
		answer.put("result", "success");
		answer.put("event_id", id);
		return ResponseEntity.ok(answer.toJSONString());
	}

	@GetMapping("/stats/date")
	public String getDateStats(@RequestParam String date, Model model) {
		User user = authHelper.getCurrentUser();
		List<SquadStats> statsList = dbservice.getStatsRepository().findByDate(date.replaceAll("-", "."));
		statsList.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model.addAttribute("statss", statsList);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/alltable")
	public String getAllStats(Model model) {
		User user = authHelper.getCurrentUser();
		List<SquadStats> statsList = dbservice.getStatsRepository().findByAuthor(user.getLogin());
		model.addAttribute("statss", statsList);
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String personalStatsTable(@RequestParam String name, Model model) {
		User user = authHelper.getCurrentUser();
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/commander/stats?error_notfound";
		}
		List<SquadStats> s = dbservice.getStatsRepository().findByHuman(human);
		s.removeIf(stats -> !stats.getAuthor().equals(user.getLogin()));
		model.addAttribute("statss", s);
		return "public/statsview_rawtable";
	}
}
