package ru.electronprod.OtryadAdmin.data.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.repositories.StatsRepository;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Stats;

import java.util.List;
import java.util.Optional;

@Service
public class StatsService {

	@Autowired
	private StatsRepository statsRepository;

	// Получение всех Stats
	@Transactional(readOnly = true)
	public List<Stats> findAll() {
		return statsRepository.findAll();
	}

	// Получение Stats по id
	@Transactional(readOnly = true)
	public Optional<Stats> findById(int id) {
		return statsRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<Stats> findByDate(String date) {
		return statsRepository.findByDate(date);
	}

	// Сохранение нового или обновлённого Stats
	@Transactional
	public Stats save(Stats stats) {
		return statsRepository.save(stats);
	}

	// Удаление Stats по объекту
	@Transactional
	public void delete(Stats stats) {
		statsRepository.delete(stats);
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
	public List<Stats> findByAuthor(String author) {
		return statsRepository.findByAuthor(author);
	}

	@Transactional(readOnly = true)
	public List<Stats> findByHuman(Human human) {
		return statsRepository.findByHuman(human);
	}

	@Transactional(readOnly = true)
	public int countByIsPresent(boolean isPresent) {
		return statsRepository.countByIsPresent(isPresent);
	}

	@Transactional
	public void saveAll(List<Stats> statsArr) {
		statsRepository.saveAll(statsArr);
	}
}
