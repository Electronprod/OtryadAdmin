package ru.electronprod.OtryadAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.electronprod.OtryadAdmin.models.Human;

public interface HumanRepository extends JpaRepository<Human, Integer> {
	@Query("SELECT COUNT(h) FROM Human h")
	int getSize();
}
