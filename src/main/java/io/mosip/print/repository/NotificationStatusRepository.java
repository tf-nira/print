package io.mosip.print.repository;

import java.util.Optional;
import io.mosip.print.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationStatusRepository extends JpaRepository<NotificationStatus, String> {

    Optional<NotificationStatus> findByNinAndTopic(String nin, String topic);
}
