package io.mosip.print.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SmsResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Response status.
     */
    private String status;

    /**
     * Response message
     */
    private String message;
}
