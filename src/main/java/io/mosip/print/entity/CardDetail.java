package io.mosip.print.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_detail", schema = "print")
public class CardDetail {
    
	@Id
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "reg_id")
    private String regId;

    @Column(name = "nin")
    private String nin;
    
    @Column(name = "given_name")
    private String givenName;
    
    @Column(name = "surname")
    private String surname;
    
    @Column(name = "other_name")
    private String otherName;
    
    @Column(name = "nationality")
    private String nationality;
    
    @Column(name = "sex")
    private String sex;
    
    @Column(name = "date_of_birth")
    private String dateOfBirth;
    
    @Column(name = "primary_finger")
    private String primaryFinger;
    
    @Column(name = "secondary_finger")
    private String secondaryFinger;
    
    @Column(name = "date_of_issue")
    private String dateOfIssue;
    
    @Column(name = "date_of_expiry")
    private String dateOfExpiry;
    
    @NotNull
	@Column(name = "cr_by")
	private String createdBy;

	@NotNull
	@Column(name = "cr_dtimes")
	private LocalDateTime crDTimes;
	
	@Column(name = "upd_by")
	private String updatedBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updatedTimes;
	
	@Column(name = "is_deleted")
	private Boolean isDeleted;
	
	@Column(name = "del_dtimes")
	private LocalDateTime deletedTimes;
 
}
