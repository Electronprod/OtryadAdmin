package ru.electronprod.OtryadAdmin.services.data;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.repositories.NewsRepository;
import ru.electronprod.OtryadAdmin.models.News;
import ru.electronprod.OtryadAdmin.models.Squad;

@Service
public class NewsService {
	@Autowired
	private NewsRepository rep;

	// Получение всех Squad
	@Transactional(readOnly = true)
	public List<News> findAll() {
		return rep.findAll();
	}

	// Получение Squad по id
	@Transactional(readOnly = true)
	public Optional<News> findById(int id) {
		return rep.findById(id);
	}

	// Сохранение нового или обновлённого News
	@Transactional
	public News save(News News) {
		return rep.save(News);
	}

	// Удаление News по объекту
	@Transactional
	public void delete(News News) {
		rep.delete(News);
	}

	// Удаление Squad по id
	@Transactional
	public void deleteById(int id) {
		rep.deleteById(id);
	}
	@Transactional(readOnly = true)
	public List<News> getLast5() {
		return rep.findTop5ByIdDesc();
	}
}
