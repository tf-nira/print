package io.mosip.print.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.print.entity.CardDetail;
import org.springframework.stereotype.Repository;

@Repository("cardDetailRepository")
public interface CardDetailRepository extends BaseRepository<CardDetail, String> {
    
}
