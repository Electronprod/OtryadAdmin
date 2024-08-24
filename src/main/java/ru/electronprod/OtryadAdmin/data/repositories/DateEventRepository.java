package ru.electronprod.OtryadAdmin.data.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.electronprod.OtryadAdmin.models.DateEvent;

@Repository
public interface DateEventRepository extends JpaRepository<DateEvent, Integer> {
	List<DateEvent> findAllByDate(String date);
}