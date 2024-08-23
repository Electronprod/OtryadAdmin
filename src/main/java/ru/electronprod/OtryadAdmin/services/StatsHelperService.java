package ru.electronprod.OtryadAdmin.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import lombok.Getter;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Stats;
import ru.electronprod.OtryadAdmin.models.helpers.PersonalStatsHelper;

@Service
public class StatsHelperService {
	@Autowired
	private OptionService optionServ;

	public Model generatePersonalStatsReport(List<Stats> personalStats, Model model) {
		Map<String, PersonalStatsHelper> visitsData = new LinkedHashMap<String, PersonalStatsHelper>();
		int visits_total = 0;
		int omissions_total = 0;
		// For each type
		for (String type : optionServ.getEvent_types().keySet()) {
			// Getting type name
			String typeName = optionServ.getEvent_types().get(type).replaceAll("\\s*\\([^()]*\\)\\s*", "");
			// Getting stats lists
			List<Stats> typedStats = personalStats.stream().filter(stats -> stats.getType().equals(type)).toList();
			List<Stats> omissionsList = typedStats.stream().filter(stats -> !stats.isPresent()).toList();
			List<Stats> visitsList = typedStats.stream().filter(stats -> stats.isPresent()).toList();
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
		model.addAttribute("visits_total", visits_total);
		model.addAttribute("omissions_total", omissions_total);
		model.addAttribute("visitsData", visitsData);
		model.addAttribute("reasons_for_absences", reasons_for_absences);
		return model;
	}

	public Model generatePersonalReport(Model model, List<Stats> statsList) {
		List<Integer> attendanceValues = new ArrayList<Integer>();
		List<Integer> omissionsValues = new ArrayList<Integer>();
		List<Map<String, Boolean>> datesResult = new ArrayList<Map<String, Boolean>>();
		Map<String, Integer> reasons_for_missing = new HashMap<String, Integer>();
		// For each type...
		for (String type : optionServ.getEvent_types().keySet()) {
			// Generating typedStats list
			List<Stats> typedStats = new ArrayList<Stats>();
			typedStats.addAll(statsList);
			typedStats.removeIf(stats -> !stats.getType().equals(type));
			// Data
			Map<String, Boolean> dateMap = new HashMap<String, Boolean>();
			int attendanceVal = 0;
			int omissionsVal = 0;
			for (Stats stats : typedStats) {
				dateMap.putIfAbsent(stats.getDate(), stats.isPresent());
				if (stats.isPresent()) {
					// Пришел
					attendanceVal++;
				} else {
					// Не пришел
					omissionsVal++;
					String reason = stats.getReason();
					if (reasons_for_missing.containsKey(reason)) {
						reasons_for_missing.put(reason, reasons_for_missing.get(reason) + 1);
					} else {
						reasons_for_missing.put(reason, 1);
					}
				}
			}
			// Sorting dates to send
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
			Map<String, Boolean> sortedDateMap = new TreeMap<>(new Comparator<String>() {
				@Override
				public int compare(String date1, String date2) {
					try {
						return dateFormat.parse(date1).compareTo(dateFormat.parse(date2));
					} catch (ParseException e) {
						throw new IllegalArgumentException(e);
					}
				}
			});
			sortedDateMap.putAll(dateMap);
			datesResult.add(sortedDateMap);
			attendanceValues.add(attendanceVal);
			omissionsValues.add(omissionsVal);
			typedStats = null;
			dateMap = null;
			sortedDateMap = null;
		}
		model.addAttribute("events_types", optionServ.getEvent_types().keySet());
		model.addAttribute("attendanceData", attendanceValues);
		model.addAttribute("omissionsValues", omissionsValues);
		model.addAttribute("datesData", datesResult);
		model.addAttribute("reasons_for_missing", reasons_for_missing);
		return model;
	}

	public List<Stats> getMissedList(List<Stats> statsList) {
		List<Stats> stats1 = new ArrayList();
		stats1.addAll(statsList);
		stats1.removeIf(stats -> stats.isPresent());
		return stats1;
	}

	public Map<String, Map<Human, Integer>> generateGlobalReport(List<Stats> allStats) {
		Map<String, Map<Human, Integer>> result = new LinkedHashMap<String, Map<Human, Integer>>();
		// For each type
		for (String type : optionServ.getEvent_types().keySet()) {
			List<Stats> typedStats = allStats.stream().filter(stats -> stats.getType().equals(type)).toList();
			Map<Human, Integer> typedStatsMap = new HashMap<Human, Integer>();
			for (Stats stats : typedStats) {
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
			result.put(optionServ.getEvent_types().get(type), sortedMap);
		}
		return result;
	}
}