package ru.electronprod.OtryadAdmin.data.services;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.repositories.HumanRepository;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.Stats;
import ru.electronprod.OtryadAdmin.models.User;

import java.util.ArrayList;
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

	@Transactional(readOnly = true)
	public List<Human> findByIds(List<Integer> ids) {
		return humanRepository.findAllById(ids);
	}

	@Transactional(readOnly = true)
	public int getSize() {
		return humanRepository.getSize();
	}

	@Transactional(readOnly = true)
	public List<Human> findBySquad(Squad squad) {
		return humanRepository.findBySquad(squad);
	}

	@Transactional
	public void saveAll(List<Human> HumanArr) {
		humanRepository.saveAll(HumanArr);
	}
}
