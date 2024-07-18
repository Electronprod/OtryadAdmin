package ru.electronprod.OtryadAdmin.data.repositories;

import org.springframework.stereotype.Repository;

import ru.electronprod.OtryadAdmin.models.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByLogin(String login);
	List<User> findAllByRole(String role);
}
