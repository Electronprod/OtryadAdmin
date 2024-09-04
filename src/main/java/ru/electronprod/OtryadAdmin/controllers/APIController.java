package ru.electronprod.OtryadAdmin.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;

import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;

@RestController
public class APIController {
	@Autowired
	OptionService optionServ;

	@GetMapping("/api/get_event_types")
	public String getEventTypes() {
		Gson gson = new Gson();
		return gson.toJson(optionServ.getEvent_types());
	}

	@GetMapping("/api/get_reasons_for_absences")
	public String getReasons_for_absences() {
		Gson gson = new Gson();
		return gson.toJson(optionServ.getReasons_for_absences());
	}

	@GetMapping("/api/getrenamerdata")
	public String getReasons_for_absences_with_additions() {
		Gson gson = new Gson();
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(optionServ.getEvent_types());
		map.putAll(optionServ.getReasons_for_absences());
		map.putAll(optionServ.getReplacements());
		map.put("error:present", "N/A");
		return gson.toJson(map);
	}
}
