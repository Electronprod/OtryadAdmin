package ru.electronprod.OtryadAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Stats;

public interface StatsRepository extends JpaRepository<Stats, Integer> {
	List<Stats> findByAuthor(String author);

	List<Stats> findByDate(String date);

	@Query("SELECT s FROM Stats s WHERE s.event_id = :eventId")
	List<Stats> findByEventId(@Param("eventId") int eventId);

	List<Stats> findByHuman(Human human);

	@Query("SELECT MAX(e.event_id) FROM Stats e")
	Integer findMaxEventIDValue();

	@Query("SELECT COUNT(s) FROM Stats s WHERE s.isPresent = :isPresent")
	int countByIsPresent(@Param("isPresent") boolean isPresent);
}
