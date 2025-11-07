package io.mosip.print.dto;

import lombok.Data;

@Data
public class EventDto {	
	private String requestId; 
	private String transactionId; 
	private String nin;
	private String status; 
	private String msg;
	private String plasticCardNumber;
	private String district;
	private String county;
	private String issuanceDate;
	private String receiverSurname;
	private String receiverOtherName;
	private String receiverGivenName;
}
