package ru.electronprod.OtryadAdmin.data.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.repositories.SquadStatsRepository;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.SquadStats;

import java.util.List;
import java.util.Optional;

@Service
public class SquadStatsService {

	@Autowired
	private SquadStatsRepository statsRepository;

	// Получение всех Stats
	@Transactional(readOnly = true)
	public List<SquadStats> findAll() {
		return statsRepository.findAll();
	}

	// Получение Stats по id
	@Transactional(readOnly = true)
	public Optional<SquadStats> findById(int id) {
		return statsRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<SquadStats> findByDate(String date) {
		return statsRepository.findByDate(date);
	}

	// Сохранение нового или обновлённого Stats
	@Transactional
	public SquadStats save(SquadStats stats) {
		return statsRepository.save(stats);
	}

	// Удаление Stats по объекту
	@Transactional
	public void delete(SquadStats stats) {
		statsRepository.delete(stats);
	}

	@Transactional
	public void deleteAll(List<SquadStats> statsList) {
		statsRepository.deleteAll(statsList);
	}

	// Удаление Stats по id
	@Transactional
	public void deleteById(int id) {
		statsRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public int findMaxEventIDValue() {
		Integer val = statsRepository.findMaxEventIDValue();
		if (val == null)
			return -1;
		return val.intValue();
	}

	@Transactional(readOnly = true)
	public List<SquadStats> findByAuthor(String author) {
		return statsRepository.findByAuthor(author);
	}

	@Transactional(readOnly = true)
	public List<SquadStats> findByHuman(Human human) {
		return statsRepository.findByHuman(human);
	}

	@Transactional(readOnly = true)
	public List<SquadStats> findByEvent_id(int id) {
		return statsRepository.findByEventId(id);
	}

	@Transactional(readOnly = true)
	public int countByIsPresent(boolean isPresent) {
		return statsRepository.countByIsPresent(isPresent);
	}

	@Transactional
	public void saveAll(List<SquadStats> statsArr) {
		statsRepository.saveAll(statsArr);
	}

	@Transactional(readOnly = true)
	public List<SquadStats> findAll(Sort by) {
		return statsRepository.findAll(by);
	}
}
