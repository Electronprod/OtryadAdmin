package ru.electronprod.OtryadAdmin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import ru.electronprod.OtryadAdmin.models.User;

@Service
public class AuthHelper {
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsrDetails details = (UsrDetails) authentication.getPrincipal();
		return details.getUser();
	}
}
