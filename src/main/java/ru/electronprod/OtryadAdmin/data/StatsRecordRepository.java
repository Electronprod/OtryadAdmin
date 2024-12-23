package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.StatsRecord;

public interface StatsRecordRepository extends JpaRepository<StatsRecord, Integer> {
	List<StatsRecord> findByAuthor(String author);

	List<StatsRecord> findByAuthor(String author, Sort sort);

	List<StatsRecord> findByDate(String date);

	List<StatsRecord> findByType(String type);

	@Query("SELECT DISTINCT s.type FROM StatsRecord s ORDER BY s.type")
	List<String> findDistinctTypes();

	List<StatsRecord> findByDateAndAuthor(String date, String author);

	List<StatsRecord> findByDateAndAuthor(String date, String author, Sort sort);

	List<StatsRecord> findByTypeAndAuthor(String type, String author);

	List<StatsRecord> findByHumanAndAuthor(Human human, String author);

	List<StatsRecord> findByHumanAndAuthor(Human human, String author, Sort sort);

	int countByDateAndIsPresent(String date, boolean isPresent);

	@Query("SELECT s FROM StatsRecord s WHERE s.event_id = :eventId")
	List<StatsRecord> findByEventId(@Param("eventId") int eventId);

	List<StatsRecord> findByHuman(Human human);

	@Query("SELECT COALESCE(MAX(e.event_id), 0) FROM StatsRecord e")
	Integer findMaxEventIDValue();

	@Query("SELECT COUNT(s) FROM StatsRecord s WHERE s.isPresent = :isPresent")
	int countByIsPresent(@Param("isPresent") boolean isPresent);

	@Query("SELECT COUNT(DISTINCT s.author) FROM StatsRecord s WHERE s.date = ?1")
	long countDistinctAuthorsByDate(String date);

	List<StatsRecord> findByHuman(Human human, Sort by);

	List<StatsRecord> findByTypeAndAuthor(String event_name, String login, Sort by);
}
