package io.mosip.print.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicDto {
	private String NIN;
	private String givenName;
	private String surname;
	private String otherNames;
	private String gender;
	private String dateOfBirth;
}
