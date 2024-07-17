package ru.electronprod.OtryadAdmin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {
	@GetMapping("/login")
	public String loginPage() {
		return "auth/login";
	}
}
