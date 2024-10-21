package ru.electronprod.OtryadAdmin.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.dto.EventTypeDTO;

@RestController
public class APIController {

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

	@GetMapping("/api/get_event_types")
	public String getEventTypes() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(SettingsRepository.convertEventTypeDTOs());
		return jsonObject.toJSONString();
	}

	@GetMapping("/api/get_reasons_for_absences")
	public String getReasons_for_absences() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(SettingsRepository.getReasons_for_absences());
		return jsonObject.toJSONString();
	}

	@GetMapping("/api/getrenamerdata")
	public String getReasons_for_absences_with_additions() {
		JSONObject jsonObject = new JSONObject();
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(SettingsRepository.convertEventTypeDTOs());
		map.putAll(SettingsRepository.getReasons_for_absences());
		map.putAll(SettingsRepository.getReplacements());
		map.put("error:present", "N/A");
		jsonObject.putAll(map);
		return jsonObject.toJSONString();
	}
}
