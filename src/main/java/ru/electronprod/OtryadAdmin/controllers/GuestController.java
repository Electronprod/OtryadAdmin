package ru.electronprod.OtryadAdmin.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
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
	public String showMainPage() {
		return "index";
	}

	@GetMapping("/public/licenses")
	public String licenses() {
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
			log.info("User logged in: " + auth.getCurrentUser().getLogin() + " (ADMIN)");
			return "redirect:/admin";
		} else if (roles.contains("ROLE_SQUADCOMMANDER")) {
			log.info("User logged in: " + auth.getCurrentUser().getLogin() + " (SQUADCOMMANDER)");
			return "redirect:/squadcommander";
		} else if (roles.contains("ROLE_OBSERVER")) {
			log.info("User logged in: " + auth.getCurrentUser().getLogin() + " (OBSERVER)");
			return "redirect:/observer";
		} else if (roles.contains("ROLE_COMMANDER")) {
			log.info("User logged in: " + auth.getCurrentUser().getLogin() + " (COMMANDER)");
			return "redirect:/commander";
		} else {
			// If user is a guest
			return "index";
		}
	}
}
