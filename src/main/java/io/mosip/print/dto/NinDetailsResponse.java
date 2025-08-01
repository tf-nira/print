package io.mosip.print.dto;

import lombok.Data;

@Data
public class NinDetailsResponse {
	private String rid;
	private DemographicDto demographics;
	private String status;
}
