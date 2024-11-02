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
import ru.electronprod.OtryadAdmin.models.SquadStats;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.EventTypeDTO;

@RestController
public class APIController {
	@Autowired
	private DBService dbservice;

	@SuppressWarnings("unchecked")
	@GetMapping("/api/get_event_types_with_reasons")
	public String getEventTypesForReasons() {
		JSONArray arr = new JSONArray();
		List<EventTypeDTO> data = SettingsRepository.getEvent_types().stream()
				.filter(dto -> dto.isCanSetReason() == true).toList();
		data.forEach(e -> {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("event", e.getEvent());
			jsonObject.put("name", e.getName());
			arr.add(jsonObject);
		});
		return arr.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/api/get_event_types")
	public String getEventTypes() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(SettingsRepository.convertEventTypeDTOs());
		return jsonObject.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/api/get_reasons_for_absences")
	public String getReasons_for_absences() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(SettingsRepository.getReasons_for_absences());
		return jsonObject.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/api/getrenamerdata")
	public String getReasons_for_absences_with_additions() {
		JSONObject jsonObject = new JSONObject();
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(SettingsRepository.convertEventTypeDTOs());
		map.putAll(SettingsRepository.getReasons_for_absences());
		map.putAll(SettingsRepository.getReplacements());
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
		List<SquadStats> statsArr = dbservice.getStatsRepository().findByDate(date.replaceAll("-", "."));
		List<User> usersFromStatsArr = statsArr.stream().map(SquadStats::getAuthor)
				.map(author -> dbservice.getUserRepository().findByLogin(author).get()).distinct().toList();
		usersFromStatsArr.forEach(user -> {
			JSONObject o = new JSONObject();
			o.put("login", user.getLogin());
			if (user.getSquad() != null)
				o.put("name", user.getSquad().getCommanderName());
			o.put("role", user.getRole());
			o.put("id", user.getId());
			JSONArray events = new JSONArray();
			List<String> eventsArr = statsArr.stream().filter(stats -> stats.getAuthor().equals(user.getLogin()))
					.map(SquadStats::getType).distinct().toList();
			o.put("events_count", eventsArr.size());
			events.addAll(eventsArr);
			o.put("events", events);
			answer.add(o);
		});
		return answer.toJSONString();
	}
}
