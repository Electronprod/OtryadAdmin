package ru.electronprod.OtryadAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.electronprod.OtryadAdmin.models.Stats;

public interface StatsRepository extends JpaRepository<Stats, Integer> {

}
