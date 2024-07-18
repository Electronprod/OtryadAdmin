package ru.electronprod.OtryadAdmin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class GuestController {
	@GetMapping("/")
	public String showMainPage() {
		return "index";
	}
}
