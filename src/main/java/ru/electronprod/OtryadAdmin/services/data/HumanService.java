package ru.electronprod.OtryadAdmin.services.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.repositories.HumanRepository;
import ru.electronprod.OtryadAdmin.models.Human;

import java.util.List;
import java.util.Optional;

@Service
public class HumanService {

	@Autowired
	private HumanRepository humanRepository;

	// Получение всех Human
	@Transactional(readOnly = true)
	public List<Human> findAll() {
		return humanRepository.findAll();
	}

	// Получение Human по id
	@Transactional(readOnly = true)
	public Optional<Human> findById(int id) {
		return humanRepository.findById(id);
	}

	// Сохранение нового или обновлённого Human
	@Transactional
	public Human save(Human human) {
		return humanRepository.save(human);
	}

	// Удаление Human по объекту
	@Transactional
	public void delete(Human human) {
		humanRepository.delete(human);
	}

	// Удаление Human по id
	@Transactional
	public void deleteById(int id) {
		humanRepository.deleteById(id);
	}
	@Transactional(readOnly = true)
	public List<Human> findAll(Sort by) {
		return humanRepository.findAll(by);
	}
}