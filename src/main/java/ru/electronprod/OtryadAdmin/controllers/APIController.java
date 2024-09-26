package ru.electronprod.OtryadAdmin.controllers;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;

@RestController
public class APIController {
	@Autowired
	OptionService optionServ;

	@GetMapping("/api/get_event_types")
	public String getEventTypes() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(optionServ.getEvent_types());
		return jsonObject.toJSONString();
	}

	@GetMapping("/api/get_reasons_for_absences")
	public String getReasons_for_absences() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(optionServ.getReasons_for_absences());
		return jsonObject.toJSONString();
	}

	@GetMapping("/api/getrenamerdata")
	public String getReasons_for_absences_with_additions() {
		JSONObject jsonObject = new JSONObject();
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(optionServ.getEvent_types());
		map.putAll(optionServ.getReasons_for_absences());
		map.putAll(optionServ.getReplacements());
		map.put("error:present", "N/A");
		jsonObject.putAll(map);
		return jsonObject.toJSONString();
	}
}
