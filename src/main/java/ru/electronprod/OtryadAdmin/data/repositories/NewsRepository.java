package ru.electronprod.OtryadAdmin.data.repositories;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.electronprod.OtryadAdmin.models.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {
    default List<News> findTop5ByIdDesc() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        return findAll(pageable).getContent();
    }
}
