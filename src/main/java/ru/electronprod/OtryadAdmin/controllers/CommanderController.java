package ru.electronprod.OtryadAdmin.controllers;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.ReportService;

@Slf4j
@Controller
@RequestMapping("/commander")
@PreAuthorize("hasAuthority('ROLE_COMMANDER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OBSERVER')")
public class CommanderController {
	@Autowired
	private ReportService statsHelper;
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;

	@GetMapping("")
	public String overview() {
		// A placeholder for the future
		return "forward:/commander/mark";
	}

	@GetMapping("/humans")
	public String getAllData(Model model) {
		model.addAttribute("humans", dbservice.getHumanService().findAll());
		model.addAttribute("user_role", authHelper.getCurrentUser().getRole());
		return "public/humans_rawtable";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("humanList", dbservice.getHumanService().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		model.addAttribute("login", user.getLogin());
		return "commander/mark";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody Map<String, Object> requestBody) {
		User user = authHelper.getCurrentUser();
		List<?> uncheckedPeopleList = (List<?>) requestBody.get("checkedPeople");
		JSONArray checkedPeopleArray = new JSONArray();
		checkedPeopleArray.addAll(uncheckedPeopleList);
		JSONObject answer = new JSONObject();
		int id = statsHelper.commander_mark(checkedPeopleArray, String.valueOf(requestBody.get("eventName")), user);
		answer.put("result", "success");
		answer.put("event_id", id);
		return ResponseEntity.ok(answer.toJSONString());
	}
}
