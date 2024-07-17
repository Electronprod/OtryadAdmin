package ru.electronprod.OtryadAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.electronprod.OtryadAdmin.models.Human;

public interface HumanRepository extends JpaRepository<Human, Integer> {

}
