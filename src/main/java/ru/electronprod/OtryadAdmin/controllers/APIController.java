package ru.electronprod.OtryadAdmin.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
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
	 * Required for demand page
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/api/observer/marks")
	@PreAuthorize("!hasAuthority('ROLE_SQUADCOMMANDER')")
	public String whomarked(@RequestParam(required = false) Optional<String> date) {
		List<StatsRecord> statsByDate = dbservice.getStatsRepository()
				.findByDate(date.isPresent() ? DBService.getStringDate(date.get()) : DBService.getStringDate());
		JSONArray answer = new JSONArray();
		statsByDate.stream().map(StatsRecord::getAuthor)
				.map(author -> dbservice.getUserRepository().findByLogin(author).get()).distinct().toList()
				.forEach(user -> {
					JSONObject o = new JSONObject();
					o.put("login", user.getLogin());
					o.put("name", user.getName());
					o.put("role", SettingsRepository.getRoles().get(user.getRole()));
					o.put("role_raw", user.getRole());
					o.put("id", user.getId());
					JSONArray events = new JSONArray();
					Supplier<Stream<StatsRecord>> authoredStats = () -> statsByDate.stream()
							.filter(stats -> stats.getAuthor().equals(user.getLogin()));
					List<String> eventsArr = authoredStats.get().map(StatsRecord::getType).distinct().toList();
					o.put("events_count", authoredStats.get().map(StatsRecord::getEvent_id).distinct().count());
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
		Map<String, Pair<String, Boolean>> data = SettingsRepository.getEvent_types();
		for (Entry<String, Pair<String, Boolean>> e : data.entrySet()) {
			if (!e.getValue().getSecond())
				continue;
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("event", e.getKey());
			arr.add(jsonObject);
		}
		return arr.toJSONString();
	}

	/**
	 * Required for mark pages
	 */
	@GetMapping("/api/check_already_marked")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> checkEventMarkToday(String event, String date) {
		int events = dbservice.getStatsRepository().countDistinctEventsByDateAndAuthorAndType(
				DBService.getStringDate(date), auth.getCurrentUser().getLogin(), event);
		if (events == 0 || events == 1)
			return ResponseEntity.ok().build();
		return ResponseEntity.status(302).body(Answer.fail(events + " events found."));
	}

	/**
	 * Required for Squadcommander and Commander stats pages
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/api/line_calendar")
	@PreAuthorize("isAuthenticated()")
	public String get_line_calendar_data(Optional<String> login) {
		JSONArray arr = new JSONArray();
		String loginValue = login.isEmpty() ? auth.getCurrentUser().getLogin()
				: dbservice.getUserRepository().findByLogin(login.orElseThrow()).orElse(auth.getCurrentUser())
						.getLogin();
		var data = dbservice.getStatsRepository().findByAuthor(loginValue);
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
				return "{\"mark\": false, \"people\": [\"" + dbservice.getHumanRepository().findAll().stream()
						.map(human -> String.valueOf(human.getId())).collect(Collectors.joining("\", \"")) + "\"]}";
			var realGroup = dbservice.getGroupRepository().findById(Integer.parseInt(group));
			return "{\"mark\":" + realGroup.get().isRequireAbsentMark() + ", \"people\": [\""
					+ realGroup.get().getHumans().stream().map(human -> String.valueOf(human.getId()))
							.collect(Collectors.joining("\", \""))
					+ "\"]}";
		} catch (Exception e) {
			return Answer
					.fail("Could not retrieve infomation about people in the group. Error message: " + e.getMessage());
		}
	}

	/**
	 * Required for Squadcommander mark page
	 */
	@GetMapping("/api/get_group_members_squadcommander")
	@PreAuthorize("hasAuthority('ROLE_SQUADCOMMANDER')")
	public String get_group_members_squadcommander(@RequestParam String groupname) {
		var user = auth.getCurrentUser();
		try {
			var squad_people = dbservice.getSquadRepository().findByCommander(user).getHumans();
			if (groupname.equals("null") || groupname == null)
				return "{\"groupid\": -1, \"people\": [\"" + squad_people.stream()
						.map(human -> String.valueOf(human.getId())).collect(Collectors.joining("\", \"")) + "\"]}";
			var realGroup = dbservice.getGroupRepository().findByName(groupname);
			squad_people.retainAll(realGroup.get().getHumans());
			return "{\"groupid\":" + realGroup.get().getId() + ", \"people\": [\"" + squad_people.stream()
					.map(human -> String.valueOf(human.getId())).collect(Collectors.joining("\", \"")) + "\"]}";
		} catch (Exception e) {
			return Answer
					.fail("Could not retrieve infomation about people in the group. Error message: " + e.getMessage());
		}
	}
}