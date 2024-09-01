package ru.electronprod.OtryadAdmin.data.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.repositories.SquadRepository;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.User;

import java.util.List;
import java.util.Optional;

@Service
public class SquadService {

	@Autowired
	private SquadRepository squadRepository;

	// Получение всех Squad
	@Transactional(readOnly = true)
	public List<Squad> findAll() {
		return squadRepository.findAll();
	}

	// Получение Squad по id
	@Transactional(readOnly = true)
	public Optional<Squad> findById(int id) {
		return squadRepository.findById(id);
	}

	// Сохранение нового или обновлённого Squad
	@Transactional
	public Squad save(Squad squad) {
		return squadRepository.save(squad);
	}

	// Удаление Squad по объекту
	@Transactional
	public void delete(Squad squad) {
		squadRepository.delete(squad);
	}

	@Transactional
	public Squad findByUser(User user) {
		return squadRepository.findByCommander(user);
	}

	// Удаление Squad по id
	@Transactional
	public void deleteById(int id) {
		squadRepository.deleteById(id);
	}
}
