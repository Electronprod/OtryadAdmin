package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.electronprod.OtryadAdmin.models.ActionRecord;

/**
 * Spring Data JPA repository for
 * {@link ru.electronprod.OtryadAdmin.models.ActionRecord}
 */
@Repository
public interface ActionRecordRepository extends JpaRepository<ActionRecord, Long> {

}
