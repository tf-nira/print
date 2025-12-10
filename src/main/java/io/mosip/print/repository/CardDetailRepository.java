package io.mosip.print.repository;

import io.mosip.print.entity.CardDetail;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardDetailRepository extends JpaRepository<CardDetail, String> {
    
	@Query(value ="SELECT * FROM print.card_detail c WHERE c.is_ready_to_push=true AND c.is_pushed=false AND c.is_failed IS NOT TRUE AND is_processing IS NOT TRUE order by c.cr_dtimes FOR UPDATE SKIP LOCKED LIMIT :fetchSize", nativeQuery = true)
	public List<CardDetail> getUnsendRecords(@Param("fetchSize") Integer fetchSize);
	
	Optional<CardDetail> findByNinAndRegId(String nin, String regId);
	
	@Modifying
	@Query(value = "UPDATE print.card_detail SET is_processing = true WHERE transaction_id IN (:ids)", nativeQuery = true)
	public void markAsProcessing(@Param("ids") List<String> ids);

	Optional<CardDetail> findByRegId(String regId);
}
