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
	public String showLKPage(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();
		List<String> roles = new ArrayList();
		for (GrantedAuthority authority : authorities) {
			roles.add(authority.getAuthority());
		}
		if (roles.contains("ROLE_ADMIN")) {
//			String user = " John Doe";
//			model.addAttribute("user", user);
			return "admin/dashboard";
		} else {
			return "index";
		}
	}
}
