package io.mosip.print.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.print.entity.CardEntity;
import org.springframework.stereotype.Repository;

@Repository("cardRepository")
public interface CardRepository extends BaseRepository<CardEntity, String> {
    
}
