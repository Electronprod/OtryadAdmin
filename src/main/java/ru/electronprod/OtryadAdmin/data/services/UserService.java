package ru.electronprod.OtryadAdmin.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.repositories.UserRepository;
import ru.electronprod.OtryadAdmin.models.User;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * It hashes password and saves user to database
	 * 
	 * @param person
	 */
	@Transactional
	public void register(User person) {
		person.setPassword(passwordEncoder.encode(person.getPassword()));
		userRepository.save(person);
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

	@Transactional(readOnly = true)
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<User> findById(int id) {
		return userRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public User findByLogin(String login) {
		return userRepository.findByLogin(login).orElse(null);
	}

	@Transactional(readOnly = true)
	public Optional<User> findByTelegram(String tg) {
		return userRepository.findByTelegram(tg);
	}

	@Transactional
	public void save(User user) {
		userRepository.save(user);
	}

	@Transactional
	public void deleteById(int id) {
		userRepository.deleteById(id);
	}
}
