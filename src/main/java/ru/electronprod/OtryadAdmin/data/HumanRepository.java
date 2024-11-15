package ru.electronprod.OtryadAdmin.data;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;

public interface HumanRepository extends JpaRepository<Human, Integer> {
	@Query("SELECT COUNT(h) FROM Human h")
	int getSize();

	Set<Human> findByIdIn(Set<Integer> ids);

	List<Human> findBySquad(Squad squad);
}
