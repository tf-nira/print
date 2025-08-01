package io.mosip.print.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicDto {
	@JsonProperty("NIN")
	private String nin;
	private String givenName;
	private String surname;
	private String otherNames;
	private String gender;
	private String dateOfBirth;
}
