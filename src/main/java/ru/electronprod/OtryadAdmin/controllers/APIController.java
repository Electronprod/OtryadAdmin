package ru.electronprod.OtryadAdmin.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
import ru.electronprod.OtryadAdmin.services.StatsProcessor;
import ru.electronprod.OtryadAdmin.utils.Answer;

@RestController
public class APIController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper auth;
	@Autowired
	private StatsProcessor statsProcessor;

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
	 * Provides information about events marked on a specific date.
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/api/observer/marks")
	@PreAuthorize("!hasAuthority('ROLE_SQUADCOMMANDER') && isAuthenticated()")
	public String whomarked(@RequestParam(required = false) Optional<String> date) {
		// Determining the date
		String records_date = date.map(DBService::getStringDate).orElseGet(DBService::getStringDate);
		// Obtaining the records for the date
		var recordsByDate = dbservice.getStatsRepository().findByDate(records_date);
		if (recordsByDate.isEmpty())
			// There is no information about the day
			return "[]";
		// Grouping the records by their authors
		Map<String, List<StatsRecord>> recordsByUser = recordsByDate.stream()
				.collect(Collectors.groupingBy(StatsRecord::getAuthor));
		// Obtaining information about users
		var users = dbservice.getUserRepository().findAllByLoginIn(recordsByUser.keySet());
		// Forming the result
		JSONArray result = new JSONArray();
		users.forEach(user -> {
			JSONObject o = new JSONObject();
			o.put("login", user.getLogin());
			o.put("name", user.getName());
			o.put("role", SettingsRepository.getRoles().get(user.getRole()));
			o.put("role_raw", user.getRole());
			o.put("id", user.getId());
			// For miscorrespondance check (JS)
			Set<String> uniqueTypes = new HashSet<>();
			Set<Integer> uniqueEventIds = new HashSet<>();
			for (StatsRecord sr : recordsByUser.get(user.getLogin())) {
				uniqueTypes.add(sr.getType());
				uniqueEventIds.add(sr.getEvent_id());
			}
			JSONArray events = new JSONArray();
			events.addAll(uniqueTypes);
			o.put("events", events);
			o.put("events_count", uniqueEventIds.size());
			result.add(o);
		});
		return result.toJSONString();
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
	public ResponseEntity<String> checkEventWasAlreadyMarked(String event, String date) {
		int events = dbservice.getStatsRepository().countDistinctEventsByDateAndAuthorAndType(
				DBService.getStringDate(date), auth.getCurrentUser().getLogin(), event);
		if (events == 0 || events == 1)
			return ResponseEntity.ok().build();
		return ResponseEntity.status(302).body(Answer.fail(events + " events found."));
	}

	/**
	 * Provides data for the pages that use calendar-line.js
	 * 
	 * @param login - presented if the user is not the marker
	 * @return JSON string in format: [{event1 info},{event2 info}...]
	 */
	@GetMapping("/api/line_calendar")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> get_line_calendar_info(Optional<String> login) {
		// Obtaining the records made by a user with the login provided
		var data = dbservice.getStatsRepository()
				.findByAuthor(login.isEmpty() ? auth.getCurrentUser().getLogin() : login.get());
		if (data.isEmpty())
			// The login is not valid or there is no info to display
			return ResponseEntity.status(404).body("[]");
		// Forming the JSON response
		return ResponseEntity.ok(statsProcessor.API_calendarLineProcess(data));
	}

	/**
	 * Provides data for the observer's page that use calendar-line.js
	 * 
	 * @return JSON string in format: [{event1 info},{event2 info}...]
	 */
	@GetMapping("/api/line_calendar_demandpage")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> get_line_calendar_info_demandPage() {
		// Obtaining the records made by a user with the login provided
		List<Integer> eventIDs = dbservice.getStatsRepository().findDistinctEventIds(Pageable.ofSize(9));
		if (eventIDs.isEmpty()) {
			// There is no information to send
			return ResponseEntity.status(204).body("[]");
		}
		var data = dbservice.getStatsRepository().findByEventIdIn(eventIDs);
		// Forming the JSON response
		return ResponseEntity.ok(statsProcessor.API_calendarLineProcess(data));
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