package ru.electronprod.OtryadAdmin.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.utils.Answer;

@RestController
public class APIController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper auth;

	/**
	 * @see stats_renamer.js
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/api/getrenamerdata")
	public String getRenamerData() {
		JSONObject jsonObject = new JSONObject();
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(SettingsRepository.getReplacements());
		map.putAll(SettingsRepository.getRoles());
		jsonObject.putAll(map);
		return jsonObject.toJSONString();
	}

	/**
	 * Required for observer overview page
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/api/observer/marks")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OBSERVER')")
	public String whomarked(@RequestParam(required = false) String date) {
		if (date == null || date.isEmpty())
			date = DBService.getStringDate();
		JSONArray answer = new JSONArray();
		List<StatsRecord> statsArr = dbservice.getStatsRepository().findByDate(date.replaceAll("-", "."));
		List<User> usersFromStatsArr = statsArr.stream().map(StatsRecord::getAuthor)
				.map(author -> dbservice.getUserRepository().findByLogin(author).get()).distinct().toList();
		usersFromStatsArr.forEach(user -> {
			JSONObject o = new JSONObject();
			o.put("login", user.getLogin());
			o.put("name", user.getName());
			o.put("role", SettingsRepository.getRoles().get(user.getRole()));
			o.put("role_raw", user.getRole());
			o.put("id", user.getId());
			JSONArray events = new JSONArray();
			List<String> eventsArr = statsArr.stream().filter(stats -> stats.getAuthor().equals(user.getLogin()))
					.map(StatsRecord::getType).distinct().toList();
			o.put("events_count", eventsArr.size());
			events.addAll(eventsArr);
			o.put("events", events);
			answer.add(o);
		});
		return answer.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/api/get_event_types_with_reasons")
	@PreAuthorize("hasAuthority('ROLE_SQUADCOMMANDER')")
	public String getEventTypesForReasons() {
		JSONArray arr = new JSONArray();
		Map<String, Boolean> data = SettingsRepository.getEvent_types();
		for (Map.Entry<String, Boolean> e : data.entrySet()) {
			if (!e.getValue())
				continue;
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("event", e.getKey());
			arr.add(jsonObject);
		}
		return arr.toJSONString();
	}

	/**
	 * Required for Squadcommander and Commander stats pages
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/api/line_calendar")
	@PreAuthorize("isAuthenticated()")
	public String get_line_calendar_data() {
		JSONArray arr = new JSONArray();
		var data = dbservice.getStatsRepository().findByAuthor(auth.getCurrentUser().getLogin(),
				Sort.by(Sort.Direction.ASC, "id"));
		Collection<StatsRecord> stats = data.stream().collect(
				Collectors.toMap(StatsRecord::getEvent_id, record -> record, (existing, replacement) -> existing))
				.values();
		stats.forEach(e -> {
			JSONObject o = new JSONObject();
			o.put("date", e.getDate());
			o.put("event", e.getType());
			o.put("eventid", e.getEvent_id());
			o.put("absent", data.stream().filter(e1 -> e1.getEvent_id() == e.getEvent_id() & !e1.isPresent()).count());
			o.put("present", data.stream().filter(e1 -> e1.getEvent_id() == e.getEvent_id() & e1.isPresent()).count());
			arr.add(o);
		});
		return arr.toJSONString();
	}

	/**
	 * Required for Commander mark page
	 */
	@GetMapping("/api/get_group_members")
	@PreAuthorize("isAuthenticated()")
	public String get_group_members(@RequestParam String group) {
		try {
			if (group.equals("null"))
				return "[\"" + dbservice.getHumanRepository().findAll().stream()
						.map(human -> String.valueOf(human.getId())).collect(Collectors.joining("\", \"")) + "\"]";
			var realGroup = dbservice.getGroupRepository().findById(Integer.parseInt(group));
			return "[\"" + realGroup.get().getHumans().stream().map(human -> String.valueOf(human.getId()))
					.collect(Collectors.joining("\", \"")) + "\"]";
		} catch (Exception e) {
			return Answer
					.fail("Could not retrieve infomation about people in the group. Error message: " + e.getMessage());
		}
	}
}
