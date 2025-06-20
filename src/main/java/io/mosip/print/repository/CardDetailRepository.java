package io.mosip.print.repository;

import io.mosip.print.entity.CardDetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CardDetailRepository extends JpaRepository<CardDetail, String> {
    
}
