package ru.electronprod.OtryadAdmin.services.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.repositories.UserRepository;
import ru.electronprod.OtryadAdmin.models.User;

@Service
public class AuthService {
	@Autowired
	private UserRepository usp;
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Saves user to database
	 * 
	 * @see ru.electronprod.OtryadAdmin.models.User
	 * @param person
	 */
	@Transactional
	public void register(User person) {
		person.setPassword(passwordEncoder.encode(person.getPassword()));
		usp.save(person);
	}

	/**
	 * Checks for violation of the "UNIQUE" parameter
	 * 
	 * @param login
	 * @return true - found user/ false - not found
	 */
	public boolean exists(String login) {
		return usp.findByLogin(login).isPresent();
	}
}
