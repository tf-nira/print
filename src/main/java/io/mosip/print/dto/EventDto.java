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
}
