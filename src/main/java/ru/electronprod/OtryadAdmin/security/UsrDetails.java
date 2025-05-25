package ru.electronprod.OtryadAdmin.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import ru.electronprod.OtryadAdmin.models.User;

/**
 * Spring Security user DTO
 */
@SuppressWarnings("serial")
public class UsrDetails implements UserDetails {
	private final User person;

	public UsrDetails(User person) {
		this.person = person;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority(person.getRole()));
	}

	@Override
	public String getPassword() {
		return person.getPassword();
	}

	@Override
	public String getUsername() {
		return person.getLogin();
	}

	public User getUser() {
		return person;
	}
}
