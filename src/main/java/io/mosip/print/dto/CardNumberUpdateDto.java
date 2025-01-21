package io.mosip.print.dto;

import lombok.Data;

@Data
public class CardNumberUpdateDto {
    private String publisher;
    private String topic;
    private String publishedOn;
    private EventData event;    
}

