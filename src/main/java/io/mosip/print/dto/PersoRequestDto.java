package io.mosip.print.dto;

import lombok.Data;

@Data
public class PersoRequestDto {

	private String transactionId;
	private String externalRequestId;
	private String cardNumber;
	private String surName;
	private String otherName;
	private String givenName;
	private String nationality;
	private String nationalityCode;
	private PersoAddressDto address;
	private String nin;
	private String dateOfBirth;
	private String dateOfIssuance;
	private String dateOfExpiry;
	private String sexCode;
	private String issuingCountryCode;
	private PersoBiometricsDto biometrics;
}
