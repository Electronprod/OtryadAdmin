package ru.electronprod.OtryadAdmin.data.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;

public interface HumanRepository extends JpaRepository<Human, Integer> {
	@Query("SELECT COUNT(h) FROM Human h")
	int getSize();

	List<Human> findBySquad(Squad squad);
}
