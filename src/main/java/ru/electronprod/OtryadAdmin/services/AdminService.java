package ru.electronprod.OtryadAdmin.services;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ru.electronprod.OtryadAdmin.data.services.NewsService;
import ru.electronprod.OtryadAdmin.data.services.UserService;
import ru.electronprod.OtryadAdmin.models.News;
import ru.electronprod.OtryadAdmin.models.User;

@Service
public class AdminService implements InitializingBean {
	@Value("${security.admin.login}")
	private String admin_login;
	@Value("${security.admin.password}")
	private String admin_password;

	@Autowired
	private UserService userService;
	@Autowired
	private NewsService newsService;

	/**
	 * Checks the existence of admin account
	 */
	@Override
	public void afterPropertiesSet() {
		if (userService.exists(admin_login) == false) {
			System.out.println("[AdminService]: administrator profile not found, creating a new one...");
			User user = new User();
			user.setRole("ROLE_ADMIN");
			user.setLogin(admin_login);
			user.setPassword(admin_password);
			userService.register(user);
		}

		System.out
				.println("[AdminService]: admin registered. Use authorization data from application.properties file.");
		if (newsService.findAll().isEmpty()) {
			System.out.println("[AdminService]: couldn't find any news. Creating a new one...");

			News news = new News();
			news.setTitle("First launch!");
			news.setAuthor("Generated automatically");
			news.setContent("In this day this application was launched for the first time.");
			newsService.createNews(news);

			System.out.println("[AdminService]: created.");
		}
	}

	public boolean isNativeAdmin(User person) {
		if (person.getLogin().equals(admin_login) && person.getRole().equals("ROLE_ADMIN")) {
			return true;
		}
		return false;
	}
}
