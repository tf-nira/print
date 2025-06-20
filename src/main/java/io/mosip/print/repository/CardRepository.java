package io.mosip.print.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.print.entity.CardEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CardRepository extends JpaRepository<CardEntity, String> {
    
}
