package ru.electronprod.OtryadAdmin.services;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.services.auth.AuthService;

@Service
public class AdminService implements InitializingBean {
	@Value("${security.admin.login}")
	private String admin_login;
	@Value("${security.admin.password}")
	private String admin_password;
	@Autowired
	private AuthService regService;

	/**
	 * Checks the existence of admin account
	 */
	@Override
	public void afterPropertiesSet() {
		if (regService.exists(admin_login) == false) {
			System.out.println("[AdminService]: administrator profile not found, creating a new one...");
			User user = new User();
			user.setRole("ROLE_ADMIN");
			user.setLogin(admin_login);
			user.setPassword(admin_password);
			regService.register(user);
		}
		System.out
				.println("[AdminService]: admin registered. Use authorization data from application.properties file.");
	}

	public boolean isNativeAdmin(User person) {
		if (person.getLogin().equals(admin_login) && person.getRole().equals("ROLE_ADMIN")) {
			return true;
		}
		return false;
	}
}
