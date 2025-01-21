package io.mosip.print.dto;

import lombok.Data;

@Data
public class EventDto {	
	private String requestId; 
	private String transactionId; 
	private String cardNumber; 
	private String status; 
	private String msg;
	private String plasticCardNumber;
}
