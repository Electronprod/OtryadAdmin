package ru.electronprod.OtryadAdmin.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
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
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.StatsWorker;
import ru.electronprod.OtryadAdmin.utils.Answer;
import ru.electronprod.OtryadAdmin.utils.FileOptions;
import ru.electronprod.OtryadAdmin.utils.SearchUtil;

@Slf4j
@Controller
@RequestMapping("/commander")
@PreAuthorize("hasAuthority('ROLE_COMMANDER')")
public class CommanderController {
	@Autowired
	private StatsWorker statsWorker;
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;

	@GetMapping("")
	public String overview(Model model) {
		return "forward:/commander/mark";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = dbservice.getUserRepository().findById(authHelper.getCurrentUser().getId()).orElseThrow();
		model.addAttribute("user", user);
		model.addAttribute("groups", user.getGroups());
		model.addAttribute("humanList",
				dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "commander/mark";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody Map<String, Object> requestBody) {
		List<?> uncheckedPeopleList = (List<?>) requestBody.get("checkedPeople");
		JSONArray checkedPeopleArray = new JSONArray();
		checkedPeopleArray.addAll(uncheckedPeopleList);
		JSONObject answer = new JSONObject();
		int id = statsWorker.commander_mark(checkedPeopleArray, String.valueOf(requestBody.get("eventName")),
				String.valueOf(requestBody.get("date")), authHelper.getCurrentUser());
		answer.put("result", "success");
		answer.put("event_id", id);
		return ResponseEntity.ok(answer.toJSONString());
	}

	@GetMapping("/markgroup")
	public String markGroup(Model model, @RequestParam int id) {
		Optional<Group> group1 = dbservice.getGroupRepository().findById(id);
		if (group1.isEmpty())
			return "forward:/commander/mark";
		model.addAttribute("group", group1.get());
		model.addAttribute("reasons", SettingsRepository.getReasons_for_absences());
		return "commander/mark_group";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/markgroup")
	public ResponseEntity<String> markGroup(@RequestBody String data) {
		User user = authHelper.getCurrentUser();
		try {
			JSONObject in = (JSONObject) FileOptions.ParseJS(data);
			// Getting group
			Optional<Group> group1 = dbservice.getGroupRepository()
					.findById(Integer.parseInt(String.valueOf(in.get("group"))));
			if (group1.isEmpty())
				return ResponseEntity.status(404).body(Answer.fail("Group not found"));
			// Getting people data
			Set<Integer> presentIDs = new HashSet<Integer>();
			presentIDs.addAll(((JSONArray) in.get("checkedPeople")));
			Map<Integer, String> unpresentIDs = new HashMap<Integer, String>();
			for (Object obj : ((JSONArray) in.get("uncheckedPeople"))) {
				JSONObject o = (JSONObject) FileOptions.ParseJS(String.valueOf(obj));
				unpresentIDs.put(Integer.parseInt(String.valueOf(o.get("id"))), String.valueOf(o.get("reason")));
			}
			// Marking
			if (!statsWorker.commander_mark(presentIDs, unpresentIDs,
					group1.get().getName() + ": " + String.valueOf(in.get("statsType")),
					String.valueOf(in.get("date")).replaceAll("-", "."), user)) {
				ResponseEntity.internalServerError().body(Answer.fail("Can't save data to DB"));
			}
			return ResponseEntity.ok(Answer.success());
		} catch (ParseException e) {
			return ResponseEntity.badRequest().body(Answer.fail("Can't parse JSON: " + e.getMessage()));
		}
	}

	@GetMapping("/stats")
	public String stats_overview() {
		return "commander/stats_overview";
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByDateAndAuthor(date.replaceAll("-", "."), user.getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/alltable")
	public String stats_allTable(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("statss", dbservice.getStatsRepository().findByAuthor(user.getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String personalStatsTable(@RequestParam String name, Model model) {
		User user = authHelper.getCurrentUser();
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/commander/stats?error_notfound";
		}
		model.addAttribute("statss", dbservice.getStatsRepository().findByHumanAndAuthor(human, user.getLogin()));
		return "public/statsview_rawtable";
	}

	@GetMapping("/humans")
	public String getHumansData(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "public/humans_rawtable";
	}
}
