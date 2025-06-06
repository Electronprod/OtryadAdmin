package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.User;

/**
 * Spring Data JPA repository for
 * {@link ru.electronprod.OtryadAdmin.models.Squad}
 */
@Repository
public interface SquadRepository extends JpaRepository<Squad, Integer> {
	Squad findByCommander(User commander);
}
