package io.mosip.print.dto;


import lombok.Data;

@Data
public class EventData {

	private String id;
    private String transactionId;
    private EventTypeDto type;
    private String timestamp;
    private String dataShareUri;
    private EventDetails data;
}
