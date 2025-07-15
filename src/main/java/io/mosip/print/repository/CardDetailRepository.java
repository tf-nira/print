package io.mosip.print.repository;

import io.mosip.print.entity.CardDetail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardDetailRepository extends JpaRepository<CardDetail, String> {
    
	@Query(value ="SELECT * FROM print.card_detail c WHERE c.is_ready_to_push=true AND c.is_pushed=false AND c.is_failed=false order by c.upd_dtimes LIMIT :fetchSize", nativeQuery = true)
	public List<CardDetail> getUnsendRecords(@Param("fetchSize") Integer fetchSize);
}
