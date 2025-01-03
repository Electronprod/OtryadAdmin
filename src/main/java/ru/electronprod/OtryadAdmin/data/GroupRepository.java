package ru.electronprod.OtryadAdmin.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.User;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
	Optional<User> findByMarker(User marker);
}
