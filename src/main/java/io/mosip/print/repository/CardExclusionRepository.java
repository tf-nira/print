package io.mosip.print.repository;

import io.mosip.print.entity.CardExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardExclusionRepository extends JpaRepository<CardExclusion, String> {

    boolean existsByRegId(String regId);
}

