package io.mosip.print.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_exclusion", schema = "print")
public class CardExclusion implements Serializable {

    @Id
    @Column(name = "reg_id")
    private String regId;

    @Column(name = "cr_dtimes")
    private LocalDateTime crDtimes;

    @Column(name = "cr_by")
    private String crBy;
}

