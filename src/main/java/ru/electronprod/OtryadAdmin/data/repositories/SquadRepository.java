package ru.electronprod.OtryadAdmin.data.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.User;

@Repository
public interface SquadRepository extends JpaRepository<Squad, Integer> {
	Optional<Squad> findByCommander(int commander);
}
