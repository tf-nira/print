package io.mosip.print.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * The persistent class Processed RegPrc print List database table.
 *
 * @author Thamaraikannan
 * @since 1.0.0
 */

@Component
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "nira_card", schema = "print")
public class CardEntity implements Serializable {
    /**
     * The Id.
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * The Json Data.
     */
    @Column(name = "json_data")
    private String jsonData;


    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "registration_id")
    private String registrationId;

    @Column(name = "status")
    private Integer status;

 
}
