package io.mosip.print.repository;

import java.util.List;
import java.util.Optional;

import io.mosip.print.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationStatusRepository extends JpaRepository<NotificationStatus, String> {

    Optional<NotificationStatus> findByNinAndTopic(String nin, String topic);

    @Query(value ="SELECT * FROM print.notification_status n WHERE n.notification_sent IS NOT TRUE AND n.is_processing IS NOT TRUE AND n.attributes IS NOT NULL AND n.remark IS NULL order by n.cr_dtimes FOR UPDATE SKIP LOCKED LIMIT :fetchSize", nativeQuery = true)
	public List<NotificationStatus> getUnnotifiedRecords(@Param("fetchSize") Integer fetchSize);
}
