package ru.electronprod.OtryadAdmin.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.User;

/**
 * Spring Data JPA repository for
 * {@link ru.electronprod.OtryadAdmin.models.Group}
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
	List<Group> findByMarker(User marker);

	List<Group> findByEditable(boolean editable);

	boolean existsByName(String name);

	Optional<Group> findByName(String name);
}
