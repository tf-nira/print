package io.mosip.print.dto;

import lombok.Data;

@Data
public class UpdateStatusResponseDto {
	
	private boolean isSuccess;
	ErrorDTO error;
}
