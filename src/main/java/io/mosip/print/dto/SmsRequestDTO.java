package io.mosip.print.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SmsRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Contact number of recipient.
     */
    private String number;

    /**
     * Message need to send.
     */
    private String message;
}
