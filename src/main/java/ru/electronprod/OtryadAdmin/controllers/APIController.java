package ru.electronprod.OtryadAdmin.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;

import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;
import ru.electronprod.OtryadAdmin.services.DateEventService;

@RestController
public class APIController {
	@Autowired
	OptionService optionServ;
	@Autowired
	private DateEventService des;

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

	@GetMapping("/api/event-calendar/daydata")
	public String getDayData(@RequestParam String day) {
		return des.getDayEvents(day).toString();
	}
}
