package ru.electronprod.OtryadAdmin.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.utils.IntPair;

/**
 * The class contains statistical methods, that are used more than once
 */
@Service
public class StatsProcessor {
	@Autowired
	private DBService dbservice;

	/**
	 * Forms table based on the user's StatsRecords that match the date and the
	 * filters
	 * 
	 * @param user    - the user, who is the creator of the StatsRecords we search
	 *                for
	 * @param date    - the date of the StatsRecords
	 * @param eventid - (Optional) - the eventID of the StatsRecords we search for
	 * @param group   - (Optional) - the group of the StatsRecords we search for
	 * @param model   - the model to add the result StatsRecords
	 * @return the path to html template
	 */
	public String getEventByDateAndFilters_user(User user, String date, Optional<String> eventid,
			Optional<String> group, Model model) {
		try {
			var data = dbservice.getStatsRepository().findByDateAndAuthor(DBService.getStringDate(date),
					user.getLogin(), Sort.by(Sort.Direction.DESC, "id"));
			if (eventid.isPresent())
				data = data.stream().filter(e -> String.valueOf(e.getEvent_id()).equals(eventid.get())).toList();
			if (group.isPresent())
				data = data.stream().filter(e -> String.valueOf(e.getGroup()).equals(group.get())).toList();
			model.addAttribute("statss", data);
		} catch (Exception e) {
		}
		return "public/statsview_rawtable";
	}

	/**
	 * Creates an event attendance report based on the user's StatsRecords
	 * 
	 * @param user       - the user
	 * @param event_name - the name of the event
	 * @param model      - the model to put the data
	 * @return the path to html template
	 */
	public String reportEvent_user(User user, String event_name, Model model) {
		List<StatsRecord> stats = dbservice.getStatsRepository().findByTypeAndAuthor(event_name, user.getLogin(),
				Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("data", getAttendanceReportByStatsRecords(stats));
		model.addAttribute("eventName", event_name);
		return "public/event_stats";
	}

	/**
	 * Generates the table of StatsRecords, which matches the event_name and the
	 * user given
	 * 
	 * @param user       - the user
	 * @param event_name - the name of the event
	 * @param model      - the model to put the data
	 * @return the path to html template
	 */
	public String showEventTable_user(User user, String event_name, Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByTypeAndAuthor(event_name, user.getLogin(),
				Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	/**
	 * Puts to the model all StatsRecords made by the user
	 * 
	 * @param user
	 * @param model - the model to put the data
	 * @return the path to html template
	 */
	public String showFullTable_user(User user, Model model) {
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByAuthor(user.getLogin(), Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	/**
	 * Forms the attendance report based on the user's marks
	 * 
	 * @param user           - the user to generate report for
	 * @param include_groups - Shell we take into account the StatsRecords related
	 *                       to some groups?
	 * @param model          - Model to add the data
	 * @return the path to html template
	 */
	public String report_user(User user, boolean include_groups, Model model) {
		var data = dbservice.getStatsRepository().findByAuthor(user.getLogin());
		if (include_groups == false)
			data.removeIf(event -> event.getGroup() != null);
		model.addAttribute("data", getAttendanceReportByStatsRecords(data));
		model.addAttribute("eventName", user.getName() + "'s all events report");
		return "public/event_stats";
	}

	/*
	 * Person methods
	 */

	/**
	 * Generates a StatsRecords table for a person based on the user's marks.
	 * 
	 * @param user  - the user
	 * @param human - the person
	 * @param model - model to add the data
	 * @return the path to html template
	 */
	public String getTableForPerson_user(User user, Human human, Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByHumanAndAuthor(human, user.getLogin(),
				Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	/*
	 * Group methods
	 */

	/**
	 * Forms the attendance report for the event related to the group
	 * 
	 * @param eventname - the event
	 * @param groupname - the group
	 * @param model     - model to add the data
	 * @return the path to html template
	 */
	public String reportGroupEvent(String eventname, String groupname, Model model) {
		model.addAttribute("data", getAttendanceReportByStatsRecords(dbservice.getStatsRepository()
				.findByTypeAndGroup(eventname, groupname, Sort.by(Sort.Direction.DESC, "id"))));
		model.addAttribute("eventName", eventname);
		return "public/event_stats";
	}

	/**
	 * Generates the table of StatsRecords, which matches the event and the group
	 * given
	 * 
	 * @param eventname - the event
	 * @param groupname - the group
	 * @param model     - model to add the data
	 * @return the path to html template
	 */
	public String showGroupEventTable(String eventname, String groupname, Model model) {
		model.addAttribute("statss", dbservice.getStatsRepository().findByTypeAndGroup(eventname, groupname,
				Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	/**
	 * Forms the attendance report for the group
	 * 
	 * @param groupname - the group
	 * @param model     - model to add the data
	 * @return the path to html template
	 */
	public String reportGroup(String groupname, Model model) {
		model.addAttribute("data", getAttendanceReportByStatsRecords(
				dbservice.getStatsRepository().findByGroup(groupname, Sort.by(Sort.Direction.DESC, "id"))));
		model.addAttribute("eventName", groupname);
		return "public/event_stats";
	}

	/**
	 * Generates the table of StatsRecords for the group
	 * 
	 * @param groupname - the group
	 * @param model     - model to add the data
	 * @return the path to html template
	 */
	public String showGroupTable(String groupname, Model model) {
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByGroup(groupname, Sort.by(Sort.Direction.DESC, "date")));
		return "public/statsview_rawtable";
	}

	public void fill_statsPage_group(Group group, Model model) {
		model.addAttribute("group", group);
		model.addAttribute("humans",
				group.getHumans().stream().sorted(Comparator.comparing(obj -> obj.getLastname())).toList());
		model.addAttribute("group_events", dbservice.getStatsRepository().findDistinctTypesGroup(group.getName()));
		model.addAttribute("reasons",
				dbservice.getStatsRepository().findByGroup(group.getName(), Sort.by(Sort.Direction.ASC, "reason"))
						.stream().map(StatsRecord::getReason)
						.filter(reason -> !SettingsRepository.getReplacements().containsKey(reason))
						.collect(Collectors.groupingBy(reason -> reason, Collectors.counting())));
	}
	/*
	 * General methods
	 */

	/**
	 * Generates attendance report based on the input statsRecords.
	 * 
	 * @param typedStats - some statistical records that need to be used to count
	 *                   visits
	 * @return sorted Map<Human, Integer>, where Integer is number of visits
	 */
	public Map<Human, Integer> getAttendanceReportByStatsRecords(List<StatsRecord> typedStats) {
		Map<Human, Integer> typedStatsMap = new HashMap<Human, Integer>();
		for (StatsRecord stats : typedStats) {
			Human human = stats.getHuman();
			typedStatsMap.putIfAbsent(human, 0);
			if (stats.isPresent()) {
				typedStatsMap.merge(human, 1, Integer::sum);
			}
		}
		return typedStatsMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	/**
	 * Generates a person report
	 * 
	 * @param person - the person
	 * @param model  - model to add the data
	 * @return the path to html template
	 */
	public String getPersonReport(Human person, Model model) {
		long time = System.currentTimeMillis();
		// Obtaining all StatsRecords from the database
		List<StatsRecord> all_records = dbservice.getStatsRepository().findByHuman(person,
				Sort.by(Sort.Direction.DESC, "id"));
		/*
		 * Result variables
		 */
		IntPair attendance = new IntPair(0, 0);
		// event, (present_num, unpresent_num)
		Map<String, IntPair> groupsData = new LinkedHashMap<String, IntPair>();
		// event, (present_num, unpresent_num)
		Map<String, IntPair> squadData = new LinkedHashMap<String, IntPair>();
		// event, (present_num, unpresent_num)
		Map<String, IntPair> commanderData = new LinkedHashMap<String, IntPair>();
		// Reason, number
		Map<String, Integer> global_reasons_data = new LinkedHashMap<String, Integer>();
		// Event, Reasons
		Map<String, List<String>> event_reasons_data = new LinkedHashMap<String, List<String>>();
		/*
		 * Counting
		 */
		for (StatsRecord statsRecord : all_records) {
			// Determines the StatsRecord category
			if (statsRecord.getGroup() != null) {
				processCountingForPersonReport(groupsData, statsRecord, attendance, global_reasons_data,
						event_reasons_data);
			} else if (statsRecord.getUser_role().equals("ROLE_SQUADCOMMANDER")) {
				processCountingForPersonReport(squadData, statsRecord, attendance, global_reasons_data,
						event_reasons_data);
			} else {
				processCountingForPersonReport(commanderData, statsRecord, attendance, global_reasons_data,
						event_reasons_data);
			}
		}
		// Adding the data to the model
		model.addAttribute("attendance", attendance);
		model.addAttribute("squadData", squadData);
		model.addAttribute("groupsData", groupsData);
		model.addAttribute("commanderData", commanderData);
		model.addAttribute("reasons_data", global_reasons_data);
		model.addAttribute("event_reasons_data", event_reasons_data);
		model.addAttribute("person", person.getLastname() + " " + person.getName());
		model.addAttribute("human", person);
		model.addAttribute("time", System.currentTimeMillis() - time);
		return "public/personal_stats";
	}

	private void processCountingForPersonReport(Map<String, IntPair> squadData, StatsRecord statsRecord,
			IntPair attendance, Map<String, Integer> global_reasons_data,
			Map<String, List<String>> event_reasons_data) {
		if (statsRecord.isPresent()) {
			squadData.merge(statsRecord.getType(), new IntPair(1, 0), (oldVal, newVal) -> {
				oldVal.add(newVal);
				return oldVal;
			});
			attendance.addFirst(1);
		} else {
			squadData.merge(statsRecord.getType(), new IntPair(0, 1), (oldVal, newVal) -> {
				oldVal.add(newVal);
				return oldVal;
			});
			attendance.addSecond(1);
			var reason = statsRecord.getReason();
			if (!reason.equals("error:unsupported_event")) {
				global_reasons_data.merge(reason, 1, Integer::sum);
				var previousValue = event_reasons_data.putIfAbsent(statsRecord.getType(),
						new ArrayList<>(List.of(reason)));
				if (previousValue != null) {
					previousValue.add(reason);
					event_reasons_data.put(statsRecord.getType(), previousValue);
				}
			}
		}
	}
}