package ru.electronprod.OtryadAdmin.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;

@RestController
public class APIController {
	@Autowired
	private DBService dbservice;

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

	@SuppressWarnings("unchecked")
	@GetMapping("/api/observer/marks")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OBSERVER')")
	public String whomarked(@RequestParam(required = false) String date, Model model) {
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
}
