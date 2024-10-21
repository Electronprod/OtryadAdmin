package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.SquadStats;

public interface SquadStatsRepository extends JpaRepository<SquadStats, Integer> {
	List<SquadStats> findByAuthor(String author);

	List<SquadStats> findByDate(String date);

	@Query("SELECT s FROM SquadStats s WHERE s.event_id = :eventId")
	List<SquadStats> findByEventId(@Param("eventId") int eventId);

	List<SquadStats> findByHuman(Human human);

	@Query("SELECT MAX(e.event_id) FROM SquadStats e")
	Integer findMaxEventIDValue();

	@Query("SELECT COUNT(s) FROM SquadStats s WHERE s.isPresent = :isPresent")
	int countByIsPresent(@Param("isPresent") boolean isPresent);
}
