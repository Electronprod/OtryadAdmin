package ru.electronprod.OtryadAdmin.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.filesystem.FileOptions;
import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;
import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.SquadStats;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.PersonalStatsHelper;

@Slf4j
@Service
public class ReportService {
	@Autowired
	private OptionService optionServ;
	@Autowired
	private DBService dbservice;

	public Model squad_generatePersonalReport(List<SquadStats> personalStats, Model model) {
		Map<String, PersonalStatsHelper> visitsData = new LinkedHashMap<String, PersonalStatsHelper>();
		int visits_total = 0;
		int omissions_total = 0;
		// For each type
		for (String type : optionServ.convertEventTypeDTOs().keySet()) {
			// Getting type name
			String typeName = optionServ.convertEventTypeDTOs().get(type).replaceAll("\\s*\\([^()]*\\)\\s*", "");
			// Getting stats lists
			List<SquadStats> typedStats = personalStats.stream().filter(stats -> stats.getType().equals(type)).toList();
			List<SquadStats> omissionsList = typedStats.stream().filter(stats -> !stats.isPresent()).toList();
			List<SquadStats> visitsList = typedStats.stream().filter(stats -> stats.isPresent()).toList();
			// Adding to total result
			visits_total = visits_total + visitsList.size();
			omissions_total = omissions_total + omissionsList.size();
			// Adding omissions/visits stats
			PersonalStatsHelper visitStats = new PersonalStatsHelper();
			visitStats.setVisits(visitsList.size());
			visitStats.setOmissions(omissionsList.size());
			visitsData.put(typeName, visitStats);
		}

		Map<String, Long> reasons_for_absences = new LinkedHashMap<String, Long>();
		// For each absence reason
		for (String reason : optionServ.getReasons_for_absences().keySet()) {
			long reasonCount = personalStats.stream().filter(stats -> stats.getReason().equals(reason)).count();
			reasons_for_absences.put(optionServ.getReasons_for_absences().get(reason), reasonCount);
		}

		// Adding visits data
		personalStats.sort(Comparator.comparingInt(SquadStats::getEvent_id).reversed());
		List<SquadStats> lastEventsData = personalStats.stream().limit(30).toList();
		model.addAttribute("visits_total", visits_total);
		model.addAttribute("omissions_total", omissions_total);
		model.addAttribute("visitsData", visitsData);
		model.addAttribute("reasons_for_absences", reasons_for_absences);
		model.addAttribute("lastEventsData", lastEventsData);
		return model;
	}

	public Map<String, Map<Human, Integer>> squad_generateGlobalReport(List<SquadStats> allStats) {
		Map<String, Map<Human, Integer>> result = new LinkedHashMap<String, Map<Human, Integer>>();
		// For each type
		for (String type : optionServ.convertEventTypeDTOs().keySet()) {
			List<SquadStats> typedStats = allStats.stream().filter(stats -> stats.getType().equals(type)).toList();
			Map<Human, Integer> typedStatsMap = new HashMap<Human, Integer>();
			for (SquadStats stats : typedStats) {
				Human human = stats.getHuman();
				if (stats.isPresent()) {
					// Человек пришел
					if (typedStatsMap.containsKey(human)) {
						typedStatsMap.put(human, typedStatsMap.get(human) + 1);
					} else {
						typedStatsMap.put(human, 1);
					}
				} else {
					// Человек не пришел
					if (!typedStatsMap.containsKey(human)) {
						typedStatsMap.put(human, 0);
					}
				}
			}
			// Sorting
			Map<Human, Integer> sortedMap = typedStatsMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
							LinkedHashMap::new));
			// Adding to result
			result.put(optionServ.convertEventTypeDTOs().get(type), sortedMap);
		}
		return result;
	}

	@Transactional
	public int squad_mark(JSONArray markedArr, String eventType, User user) throws ParseException {
		List<SquadStats> resultArray = new ArrayList<SquadStats>();
		int event_id = dbservice.getStatsService().findMaxEventIDValue() + 1;
		// People managed by this user
		List<Human> humans = dbservice.getSquadService().findByUser(user).getHumans();
		// Those who didn't come
		for (Object o : markedArr) {
			JSONObject data = (JSONObject) FileOptions.ParseJsThrought(String.valueOf(o));
			// Finding human to add stats record
			Human human1 = humans.stream()
					.filter(human -> human.getId() == Integer.parseInt(String.valueOf(data.get("id")))).findFirst()
					.orElseThrow();
			SquadStats stats = new SquadStats(human1);
			stats.setAuthor(user.getLogin());
			stats.setDate(DBService.getStringDate());
			stats.setPresent(false);
			stats.setReason(String.valueOf(data.get("reason")));
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
		dbservice.getStatsService().saveAll(resultArray);
		log.info("User " + user.getLogin() + " marked " + resultArray.size() + " people. EventID: " + event_id);
		return event_id;
	}
//	@Transactional
//	public void squad_mark(MarkDTO detail, String eventType, User user) throws Exception {
//		// TODO eventType check and search
//		Map<Integer, String> details1 = detail.getDetails(); // human ID + Reason
//		List<SquadStats> resultArray = new ArrayList<SquadStats>(); // Result we will add to database
//		int event_id = dbservice.getStatsService().findMaxEventIDValue() + 1;
//		// People managed by this user
//		List<Human> humans = dbservice.getSquadService().findByUser(user).getHumans();
//		// Those who didn't come
//		details1.forEach((id, reason) -> {
//			Human human1 = humans.stream().filter(human -> human.getId() == id).findFirst().orElseThrow();
//			SquadStats stats = new SquadStats(human1);
//			stats.setAuthor(user.getLogin());
//			stats.setDate(DBService.getStringDate());
//			stats.setPresent(true);
//			stats.setReason("error:present");
//			stats.setType(eventType);
//			stats.setUser_role(user.getRole());
//			stats.setEvent_id(event_id);
//			resultArray.add(stats);
//			humans.remove(human1);
//		});
//		// Those who come
//		for (Human human : humans) {
//			SquadStats stats = new SquadStats(human);
//			stats.setAuthor(user.getLogin());
//			stats.setDate(DBService.getStringDate());
//			stats.setPresent(false);
//			stats.setReason(reason);
//			stats.setType(eventType);
//			stats.setUser_role(user.getRole());
//			stats.setEvent_id(event_id);
//			resultArray.add(stats);
//		}
//		// Saving result to database
//		dbservice.getStatsService().saveAll(resultArray);
//		log.info("User " + user.getLogin() + " marked " + resultArray.size() + " people. EventID: " + event_id);
//	}
}
