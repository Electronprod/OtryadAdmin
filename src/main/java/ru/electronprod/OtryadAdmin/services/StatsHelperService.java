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

@Service
public class StatsHelperService {
	@Autowired
	private OptionService optionServ;

	public Model generatePersonalReport(Model model, List<Stats> statsList) {
		List<Integer> attendanceValues = new ArrayList<Integer>();
		List<Integer> omissionsValues = new ArrayList<Integer>();
		List<Map<String, Boolean>> datesResult = new ArrayList<Map<String, Boolean>>();
		Map<String, Integer> reasons_for_missing = new HashMap<String, Integer>();
		// For each type...
		for (String type : events_types) {
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
		model.addAttribute("events_types", events_types);
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
