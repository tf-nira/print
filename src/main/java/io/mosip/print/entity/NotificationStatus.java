package io.mosip.print.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_status", schema = "print")
@IdClass(NotificationStatusId.class)
public class NotificationStatus {

    @Id
    @Column(name = "nin")
    private String nin;

    @Id
    @Column(name = "topic")
    private String topic;

    @Column(name = "notification_sent")
    private boolean notificationSent;

    @NotNull
    @Column(name = "cr_dtimes")
    private LocalDateTime crDTimes;

    @Column(name = "upd_dtimes")
    private LocalDateTime updatedTimes;
}
