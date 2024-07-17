package ru.electronprod.OtryadAdmin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticController {
	@GetMapping("/")
	public String showMainPage() {
		return "index";
	}
}
