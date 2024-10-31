package ru.electronprod.OtryadAdmin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.UserRepository;
import ru.electronprod.OtryadAdmin.models.User;

@Service
public class AuthHelper {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Returns User object from SpringContextHolder
	 * 
	 * @return user
	 */
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsrDetails details = (UsrDetails) authentication.getPrincipal();
		return details.getUser();
	}

	/**
	 * It hashes password and saves user to database
	 * 
	 * @param person
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
