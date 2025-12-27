package ru.electronprod.OtryadAdmin.collector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CandidateRepository extends JpaRepository<Candidate, Integer> {
}
