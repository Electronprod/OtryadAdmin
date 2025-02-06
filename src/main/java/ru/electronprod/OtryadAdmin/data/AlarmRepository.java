package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.electronprod.OtryadAdmin.models.Alarm;

public interface AlarmRepository extends JpaRepository<Alarm, Integer> {

}
