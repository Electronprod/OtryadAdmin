package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.electronprod.OtryadAdmin.models.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
}
