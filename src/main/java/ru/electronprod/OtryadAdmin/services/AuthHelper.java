package ru.electronprod.OtryadAdmin.services;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.UserRepository;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.security.UsrDetails;

/**
 * Helps to work with auth system
 */
@Slf4j
@Service
public class AuthHelper implements InitializingBean {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	// Data from application.properties
	@Value("${security.admin.login}")
	private String admin_login;
	@Value("${security.admin.password}")
	private String admin_password;

	/**
	 * Checks for ADMIN account on startup
	 */
	@Override
	public void afterPropertiesSet() {
		if (userRepository.findAll().stream().filter(user -> user.getRole().equals("ROLE_ADMIN")).toList().isEmpty()) {
			log.info("Administrator profile not found, creating a new one...");
			User user = new User();
			user.setRole("ROLE_ADMIN");
			user.setLogin(admin_login);
			user.setPassword(admin_password);
			user.setName("Administrator");
			register(user);
			log.info("Admin registered. Use authorization credentials from application.properties file.");
		}
	}

	/**
	 * Extracts user from cookies using SpringContextHolder
	 * 
	 * @return User object
	 */
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsrDetails details = (UsrDetails) authentication.getPrincipal();
		return details.getUser();
	}

	/**
	 * It hashes password and saves user to database
	 * 
	 * @apiNote This method receives plain password using person.getPassword() and
	 *          then encodes it and saves using setter.
	 * @param person - user object to work with
	 * @return boolean success/fail (true/false)
	 */
	@Transactional
	public boolean register(User person) {
		person.setPassword(passwordEncoder.encode(person.getPassword()));
		return userRepository.save(person) != null;
	}

	/**
	 * Checks login for availability
	 * 
	 * @param login
	 * @return true - found user/ false - not found
	 */
	@Transactional(readOnly = true)
	public boolean exists(String login) {
		return userRepository.findByLogin(login).isPresent();
	}
}