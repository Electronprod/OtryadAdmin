package ru.electronprod.OtryadAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.electronprod.OtryadAdmin.models.Squad;

@Repository
public interface SquadRepository extends JpaRepository<Squad, Integer> {
}
