package ru.electronprod.OtryadAdmin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.electronprod.OtryadAdmin.models.DateEvent;
import ru.electronprod.OtryadAdmin.services.DateEventService;

@Controller
public class GuestController {
	@Autowired
	private DateEventService des;
	@Value("${eventcalendar.secretkey}")
	private String secret;

	@GetMapping("/")
	public String showMainPage() {
		return "index";
	}

	@GetMapping("/public/event-calendar")
	public String event_calendar() {
		return "/public/event-calendar.html";
	}

	@GetMapping("/public/event-calendar/save")
	public String recordData(@RequestParam("name") String name, @RequestParam("content") String content,
			@RequestParam("date") String date) {
		try {
			des.addDateEvent(new DateEvent(date, content, name));
			return "redirect:/public/event-calendar?success";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/public/event-calendar?fail";
	}

	@GetMapping("/public/event-calendar/report")
	public String report(@RequestParam("key") String key, Model model) {
		if (!key.equals(secret)) {
			return "redirect:/public/event-calendar?fail";
		} else {
			model.addAttribute("dateEvents", des.getAllData());
		}
		return "/public/event-calendar-report.html";
	}

	@GetMapping("/public/licenses")
	public String licenses() {
		return "/public/licenses.html";
	}
}
