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

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import lombok.Getter;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Stats;

@Service
public class StatsHelperService {
	@Getter
	private String[] events_types = { "general", "duty", "walk", "other1", "other2" };
	@Getter
	private String[] reasons_for_missing_types = { "ill", "away", "study", "respect", "disrespect" };

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
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
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

	public Model generateGeneralReport(Model model, List<Stats> statsList) {
		for (String type : events_types) {
			List<Stats> typedStats = new ArrayList<Stats>();
			typedStats.addAll(statsList);
			// Deleting other types from list
			typedStats.removeIf(stats -> !stats.getType().equals(type));
			Map<Human, Integer> map = new HashMap<Human, Integer>();
			// For each Stats object
			for (Stats stats : typedStats) {
				Human human = stats.getHuman();
				if (stats.isPresent()) {
					// Человек пришел
					// Adding to map
					if (map.containsKey(human)) {
						map.put(human, map.get(human) + 1);
					} else {
						map.put(human, 1);
					}
				} else {
					// Человек не пришел
					if (!map.containsKey(human)) {
						map.put(human, 0);
					}
				}
			}
			// Sorting
			Map<Human, Integer> sortedMap = map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			model.addAttribute(type + "Entry", sortedMap);
			typedStats = null;
		}
		return model;
	}
}
