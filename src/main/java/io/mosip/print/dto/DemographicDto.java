package io.mosip.print.dto;

import lombok.Data;

@Data
public class DemographicDto {
	private String NIN;
	private JsonValue[] givenName;
	private JsonValue[] surname;
	private JsonValue[] otherNames;
	private JsonValue[] gender;
	private String dateOfBirth;
}
