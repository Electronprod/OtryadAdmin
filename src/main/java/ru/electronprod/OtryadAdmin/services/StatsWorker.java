package ru.electronprod.OtryadAdmin.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.SquadStats;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.utils.FileOptions;

@Slf4j
@Service
public class StatsWorker {
	@Autowired
	private DBService dbservice;

	/**
	 * Generates Map(event, Map(Human, Integer)) visitor statistics for registered
	 * event types for squad
	 * 
	 * @param records - stats records to work with
	 * @return Map(String, Map(Human, Integer))
	 */
	public Map<String, Map<Human, Integer>> squad_getEventsReport(List<SquadStats> records) {
		Map<String, Map<Human, Integer>> result = new LinkedHashMap<String, Map<Human, Integer>>();
		// For each registered type
		for (String type : SettingsRepository.convertEventTypeDTOs().keySet()) {
			// Getting stats records with this type
			List<SquadStats> typedStats = records.stream().filter(stats -> stats.getType().equals(type)).toList();
			// Declaring type's result variable
			Map<Human, Integer> typedStatsMap = new HashMap<Human, Integer>();
			// Adding data to typedStatsMap
			for (SquadStats stats : typedStats) {
				Human human = stats.getHuman();
				typedStatsMap.putIfAbsent(human, 0);

				if (stats.isPresent()) {
					typedStatsMap.merge(human, 1, Integer::sum);
				}
			}
			// Sorting it
			Map<Human, Integer> sortedMap = typedStatsMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
							LinkedHashMap::new));
			// Adding to result
			result.put(SettingsRepository.convertEventTypeDTOs().get(type), sortedMap);
			records.removeAll(typedStats);
		}
		return result;
	}

	/**
	 * Generates personal stats report from the List given
	 * 
	 * @param personalStats - person's stats records
	 * @param model         - Model to add data
	 * @apiNote <b>Added attributes:</b><br>
	 *          "attendance" - Pair(present_count, notpresent_count) "eventsData" -
	 *          Map(Strig)
	 */
	public void getMainPersonalReportModel(List<SquadStats> personalStats, Model model) {
		// Getting present and non present list
		List<SquadStats> present = personalStats.stream().filter(stats -> stats.isPresent()).toList();
		List<SquadStats> notPresent = personalStats.stream().filter(stats -> !stats.isPresent()).toList();
		// Adding visits & omissions data
		model.addAttribute("attendance", Pair.of(present.size(), notPresent.size()));
		/*
		 * Generating events attendance sheet
		 */
		// Getting all events from List
		List<String> squadEvents = personalStats.stream()
				.filter(stats -> stats.getUser_role().equals("ROLE_SQUADCOMMANDER")).map(SquadStats::getType)
				.collect(Collectors.toList());
		List<String> commanderEvents = personalStats.stream()
				.filter(stats -> stats.getUser_role().equals("ROLE_COMMANDER")).map(SquadStats::getType)
				.collect(Collectors.toList());
		// There should always be the same order
		Collections.sort(squadEvents);
		Collections.sort(commanderEvents);
		Map<String, Pair<Long, Long>> squadData = new LinkedHashMap<String, Pair<Long, Long>>();
		Map<String, Long> commanderData = new LinkedHashMap<String, Long>();
		// For each squad event type...
		squadEvents.forEach(event -> {
			// Adding stats data about this event
			squadData.put(event, Pair.of(present.stream().filter(stats -> stats.getType().equals(event)).count(),
					notPresent.stream().filter(stats -> stats.getType().equals(event)).count()));
		});
		model.addAttribute("squadData", squadData);
		// For each commander event type...
		commanderEvents.forEach(event -> {
			// Adding stats data about this event
			commanderData.put(event, present.stream().filter(stats -> stats.getType().equals(event)).count());
		});
		model.addAttribute("commanderData", commanderData);
		/*
		 * Generating table of reasons for absences
		 */
		Map<String, Integer> reasons_data = new TreeMap<String, Integer>();
		for (SquadStats stats : notPresent) {
			String reason = stats.getReason();
			if (reason.equals("error:present") || reason.equals("error:unsupported_event"))
				continue;
			reasons_data.put(reason, reasons_data.getOrDefault(reason, 0) + 1);
		}
		model.addAttribute("reasons_data", reasons_data);
		/*
		 * Adding a few stats records
		 */
		List<SquadStats> lastRecords = personalStats.stream().limit(30).collect(Collectors.toList());
		lastRecords.sort(Comparator.comparingInt(SquadStats::getEvent_id).reversed());
		model.addAttribute("lastRecords", lastRecords);
	}

	/**
	 * Mark method for commanders. Marks ONLY present people.
	 * 
	 * @param ids       - people IDs to mark
	 * @param eventName - event name
	 * @param date      - date in format "yyyy.mm.dd" or "yyyy-mm-dd"
	 * @param user      - user who marks
	 * @return EventID of this event in database
	 */
	public int commander_mark(JSONArray ids, String eventName, String date, User user) {
		List<SquadStats> resultArray = new ArrayList<SquadStats>();
		int event_id = dbservice.getStatsRepository().findMaxEventIDValue() + 1;
		List<Human> humans = dbservice.getHumanRepository().findAll();
		for (Object o : ids) {
			int human_id = Integer.parseInt(String.valueOf(o));
			Human human1 = humans.stream().filter(human -> human.getId() == human_id).findFirst().orElseThrow();
			SquadStats stats = new SquadStats(human1);
			stats.setAuthor(user.getLogin());
			stats.setDate(date.replaceAll("-", "."));
			stats.setPresent(true);
			stats.setReason("error:present");
			stats.setType(eventName);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			resultArray.add(stats);
			humans.remove(human1);
		}
		dbservice.getStatsRepository().saveAll(resultArray);
		log.info("User " + user.getLogin() + " marked " + resultArray.size() + " people. EventID: " + event_id);
		return event_id;
	}

	/**
	 * Mark method for Squadcommanders. Marks absent from the given list, marks the
	 * rest as present
	 * 
	 * @param markedArr - JSONArray with absent people objects (id,reason)
	 * @param eventType - event type to write
	 * @param user      - user who marks
	 * @return eventID of this event in database
	 * @throws ParseException - if JSONArray is damaged or invalid
	 */
	@PreAuthorize("hasAuthority('ROLE_SQUADCOMMANDER')")
	@Transactional
	public int squad_mark(JSONArray markedArr, String eventType, User user) throws ParseException {
		List<SquadStats> resultArray = new ArrayList<SquadStats>();
		Map<String, String> availableEventsForReasons = SettingsRepository.convertEventTypeDTOs(
				SettingsRepository.getEvent_types().stream().filter(dto -> dto.isCanSetReason() == true).toList());
		int event_id = dbservice.getStatsRepository().findMaxEventIDValue() + 1;
		// People managed by this user
		List<Human> humans = dbservice.getSquadRepository().findByCommander(user).getHumans();
		boolean shouldSaveReason = availableEventsForReasons.containsKey(eventType);
		if (shouldSaveReason == false)
			shouldSaveReason = !SettingsRepository.convertEventTypeDTOs().containsKey(eventType);
		// Those who didn't come
		for (Object o : markedArr) {
			JSONObject data = (JSONObject) FileOptions.ParseJS(String.valueOf(o));
			// Finding human to add stats record
			Human human1 = humans.stream()
					.filter(human -> human.getId() == Integer.parseInt(String.valueOf(data.get("id")))).findFirst()
					.orElseThrow();
			SquadStats stats = new SquadStats(human1);
			stats.setAuthor(user.getLogin());
			stats.setDate(DBService.getStringDate());
			stats.setPresent(false);
			if (shouldSaveReason) {
				stats.setReason(String.valueOf(data.get("reason")));
			} else {
				stats.setReason("error:unsupported_event");
			}
			stats.setType(eventType);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			resultArray.add(stats);
			humans.remove(human1);
		}
		// Those who come
		for (Human human : humans) {
			SquadStats stats = new SquadStats(human);
			stats.setAuthor(user.getLogin());
			stats.setDate(DBService.getStringDate());
			stats.setPresent(true);
			stats.setReason("error:present");
			stats.setType(eventType);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			resultArray.add(stats);
		}
		dbservice.getStatsRepository().saveAll(resultArray);
		log.info("Squadcommander " + user.getLogin() + " marked " + resultArray.size() + " people. EventID: "
				+ event_id);
		return event_id;
	}
}
