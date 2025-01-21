package io.mosip.print.dto;

import lombok.Data;

@Data 
public class CardUpdateRequestDto {
	private String topic;
	private String publishedOn;
	private EventDto event;
}
