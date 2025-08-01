package io.mosip.print.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NinDetailsResponse {
	private String rid;
	private DemographicDto demographics;
	private String status;
}
