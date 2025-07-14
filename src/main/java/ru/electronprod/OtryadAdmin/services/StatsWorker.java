package ru.electronprod.OtryadAdmin.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.MarkDTO;
import ru.electronprod.OtryadAdmin.telegram.BotService;
import ru.electronprod.OtryadAdmin.utils.FileOptions;

@Slf4j
@Service
public class StatsWorker {
	@Autowired
	private DBService dbservice;
	@Autowired
	private BotService botServ;

	/**
	 * Generates report for event
	 * 
	 * @param typedStats - event's statsRecords
	 * @return Map<Human, Integer>, where Integer is number of visits
	 */
	public Map<Human, Integer> getEventReport(List<StatsRecord> typedStats) {
		Map<Human, Integer> typedStatsMap = new HashMap<Human, Integer>();
		for (StatsRecord stats : typedStats) {
			Human human = stats.getHuman();
			typedStatsMap.putIfAbsent(human, 0);

			if (stats.isPresent()) {
				typedStatsMap.merge(human, 1, Integer::sum);
			}
		}
		Map<Human, Integer> sortedMap = typedStatsMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}

	/**
	 * Generates personal stats report from the List given
	 * 
	 * @param personalStats    - person's stats records
	 * @param model            - Model to add data
	 * @param processNotSquads - Shell we use data from all commanders (true) or we
	 *                         should use only information given by squadcommander
	 *                         (false)
	 */
	public void getMainPersonalReportModel(List<StatsRecord> personalStats, Model model, boolean processNotSquads) {
		// Getting present and non present list
		List<StatsRecord> present = personalStats.stream().filter(stats -> stats.isPresent()).toList();
		List<StatsRecord> notPresent = personalStats.stream().filter(stats -> !stats.isPresent()).toList();
		// Adding visits & omissions data
		model.addAttribute("attendance", Pair.of(present.size(), notPresent.size()));
		/*
		 * Generating events attendance sheet
		 */
		// Getting all events from List
		List<String> squadEvents = personalStats.stream()
				.filter(stats -> stats.getUser_role().equals("ROLE_SQUADCOMMANDER")).map(StatsRecord::getType)
				.collect(Collectors.toList());
		List<String> commanderEvents = personalStats.stream()
				.filter(stats -> !stats.getUser_role().equals("ROLE_SQUADCOMMANDER")).map(StatsRecord::getType)
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
		if (processNotSquads) {
			// For each commander event type...
			for (String event : commanderEvents) {
				Stream<StatsRecord> str = personalStats.stream().filter(stats -> stats.getType().equals(event));
				if (str.allMatch(stats -> stats.getGroup() != null)) {
					continue;
				}
				// Adding stats data about this event
				commanderData.put(event, present.stream().filter(stats -> stats.getType().equals(event)).count());
				str.close();
			}
			model.addAttribute("commanderData", commanderData);
			// Groups
			Set<String> humanGroups = personalStats.stream().map(StatsRecord::getGroup).collect(Collectors.toSet());
			Map<String, Pair<Long, Long>> groupsData = new LinkedHashMap<String, Pair<Long, Long>>();
			humanGroups.forEach(gr -> {
				groupsData.put(gr,
						Pair.of(present.stream().filter(stats -> Objects.equals(stats.getGroup(), gr)).count(),
								notPresent.stream().filter(stats -> Objects.equals(stats.getGroup(), gr)).count()));
			});
			model.addAttribute("groupsData", groupsData);
		}
		/*
		 * Generating table of reasons for absences
		 */
		Map<String, Integer> reasons_data = new TreeMap<String, Integer>();
		for (StatsRecord stats : notPresent) {
			String reason = stats.getReason();
			if (reason.equals("error:present") || reason.equals("error:unsupported_event"))
				continue;
			reasons_data.put(reason, reasons_data.getOrDefault(reason, 0) + 1);
		}
		model.addAttribute("reasons_data", reasons_data);
	}

	@Transactional(readOnly = true)
	public List<StatsRecord> generatePresentStats(Collection<Human> humans, String event_type, String formatted_date,
			User user, int event_id, String group) {
		List<StatsRecord> resultArray = new ArrayList<StatsRecord>();
		for (Human human : humans) {
			StatsRecord stats = new StatsRecord(human);
			stats.setAuthor(user.getLogin());
			stats.setDate(formatted_date);
			stats.setPresent(true);
			stats.setReason("error:present");
			stats.setType(event_type);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			stats.setGroup(group);
			resultArray.add(stats);
		}
		return resultArray;
	}

	@Transactional(readOnly = true)
	private StatsRecord createUnpresentStatsRecord(JSONObject data, String event, String formatted_date, User user,
			int event_id, List<Human> humans, String group) {
		// Finding human to add stats record
		Human human = humans.stream().filter(h -> h.getId() == Integer.parseInt(String.valueOf(data.get("id"))))
				.findFirst().orElseThrow(() -> new IllegalStateException("Human not found: " + data.get("id")));
		// Creating stats record
		StatsRecord stats = new StatsRecord(human);
		stats.setAuthor(user.getLogin());
		stats.setDate(formatted_date);
		stats.setPresent(false);
		stats.setReason(String.valueOf(data.get("reason")));
		stats.setType(event);
		stats.setUser_role(user.getRole());
		stats.setEvent_id(event_id);
		stats.setGroup(group);
		humans.remove(human);
		return stats;
	}

	@Transactional()
	public int mark_group(MarkDTO dto, User user, List<Human> humans, String group) throws Exception {
		// Validating input
		String date = DBService.getStringDate(dto.getDate() != null ? dto.getDate() : DBService.getStringDate());
		String event;
		if (dto.getEvent() != null) {
			event = dto.getEvent();
		} else {
			throw new IllegalArgumentException("Event is null");
		}
		// Defining variables
		List<StatsRecord> resultRecords = new ArrayList<StatsRecord>();
		int event_id = dbservice.getStatsRepository().findMaxEventIDValue() + 1;
		// Marking unPresent humans
		for (Object o : dto.getUnpresentPeople()) {
			JSONObject data = (JSONObject) FileOptions.ParseJS(String.valueOf(o));
			resultRecords.add(createUnpresentStatsRecord(data, event, date, user, event_id, humans, group));
		}
		// Marking Present humans
		resultRecords.addAll(generatePresentStats(humans, event, date, user, event_id, group));

		// Saving records to database
		if (dbservice.getStatsRepository().saveAll(resultRecords) == null)
			throw new Exception("Error saving stats records to database!");
		log.info("User " + user.getLogin() + " (" + user.getRole() + ") marked " + resultRecords.size()
				+ " people. EventID: " + event_id);
		botServ.sendNotification_marked(user, event);
		return event_id;
	}

	@Transactional()
	public int mark_only_present(MarkDTO dto, User user) throws Exception {
		// Validating input
		String date = DBService.getStringDate(dto.getDate() != null ? dto.getDate() : DBService.getStringDate());
		String event;
		if (dto.getEvent() != null) {
			event = dto.getEvent();
		} else {
			throw new IllegalArgumentException("Event is null");
		}
		// Defining variables
		List<StatsRecord> resultRecords = new ArrayList<StatsRecord>();
		int event_id = dbservice.getStatsRepository().findMaxEventIDValue() + 1;
		List<Human> humans = dbservice.getHumanRepository().findAll();
		for (Object o : dto.getPresentPeople()) {
			int human_id = Integer.parseInt(String.valueOf(o));
			Human human = humans.stream().filter(human1 -> human1.getId() == human_id).findFirst().orElseThrow();
			StatsRecord stats = new StatsRecord(human);
			stats.setAuthor(user.getLogin());
			stats.setDate(date);
			stats.setPresent(true);
			stats.setReason("error:present");
			stats.setType(event);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			resultRecords.add(stats);
			humans.remove(human);
		}
		// Saving records to database
		if (dbservice.getStatsRepository().saveAll(resultRecords) == null)
			throw new Exception("Error saving stats records to database!");
		log.info("User " + user.getLogin() + " (" + user.getRole() + ") marked " + resultRecords.size()
				+ " people. EventID: " + event_id);
		botServ.sendNotification_marked(user, event);
		return event_id;
	}
}
