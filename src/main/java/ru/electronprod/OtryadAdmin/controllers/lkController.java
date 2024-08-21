package ru.electronprod.OtryadAdmin.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class lkController {
	@GetMapping("/lk")
	public String redirectToHome(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		@SuppressWarnings("unchecked")
		List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();
		List<String> roles = new ArrayList<String>();
		for (GrantedAuthority authority : authorities) {
			roles.add(authority.getAuthority());
		}
		// Redirecting to home page
		if (roles.contains("ROLE_ADMIN")) {
			return "redirect:/admin";
		} else if (roles.contains("ROLE_SQUADCOMMANDER")) {
			return "redirect:/squadcommander";
		} else if (roles.contains("ROLE_OBSERVER")) {
			return "redirect:/observer";
		} else {
			// If user is guest
			return "index";
		}
	}
}
