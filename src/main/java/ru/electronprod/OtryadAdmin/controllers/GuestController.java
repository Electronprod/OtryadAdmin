package ru.electronprod.OtryadAdmin.controllers;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.services.AuthHelper;

@Controller
@Slf4j
public class GuestController {
	@Autowired
	private AuthHelper auth;

	@GetMapping("/auth/login")
	public String loginPage() {
		return "auth/login";
	}

	@GetMapping("/")
	public String showMainPage(Model model) {
		model.addAttribute("year", Year.now().getValue());
		return "index";
	}

	@GetMapping("/public/licenses")
	public String licenses(Model model) {
		model.addAttribute("year", Year.now().getValue());
		return "public/licenses";
	}

	@GetMapping("/lk")
	public String redirectToHome() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		@SuppressWarnings("unchecked")
		List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();
		List<String> roles = new ArrayList<String>();
		for (GrantedAuthority authority : authorities) {
			roles.add(authority.getAuthority());
		}
		// Redirecting to home page
		if (roles.contains("ROLE_ADMIN")) {
			log.info("Authed admin: " + auth.getCurrentUser().getLogin());
			return "redirect:/admin";
		} else if (roles.contains("ROLE_SQUADCOMMANDER")) {
			log.info("Authed squadcommander: " + auth.getCurrentUser().getLogin());
			return "redirect:/squadcommander";
		} else if (roles.contains("ROLE_OBSERVER")) {
			log.info("Authed observer: " + auth.getCurrentUser().getLogin());
			return "redirect:/observer";
		} else if (roles.contains("ROLE_COMMANDER")) {
			log.info("Authed commander: " + auth.getCurrentUser().getLogin());
			return "redirect:/commander";
		} else {
			// If user is guest
			return "index";
		}
	}
}
