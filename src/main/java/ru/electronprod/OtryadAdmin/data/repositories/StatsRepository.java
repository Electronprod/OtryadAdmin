package ru.electronprod.OtryadAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Stats;

public interface StatsRepository extends JpaRepository<Stats, Integer> {
	List<Stats> findByAuthor(String author);

	List<Stats> findByHuman(Human human);
}
