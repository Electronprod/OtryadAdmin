package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.electronprod.OtryadAdmin.models.Human;

/**
 * Spring Data JPA repository for
 * {@link ru.electronprod.OtryadAdmin.models.Human}
 */
@Repository
public interface HumanRepository extends JpaRepository<Human, Integer> {
	@Query("SELECT COUNT(h) FROM Human h")
	int getSize();
}
