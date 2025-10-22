package io.mosip.print.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmailResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * The status.
     */
    private String status;

    /**
     * The message.
     */
    private String message;
}
