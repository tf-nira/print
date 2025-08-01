package io.mosip.print.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicDto {
	private String NIN;
	private JsonValue[] givenName;
	private JsonValue[] surname;
	private JsonValue[] otherNames;
	private JsonValue[] gender;
	private String dateOfBirth;
}
