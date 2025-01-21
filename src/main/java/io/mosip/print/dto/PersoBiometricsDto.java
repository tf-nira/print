package io.mosip.print.dto;

import lombok.Data;

@Data
public class PersoBiometricsDto {	
	private String faceImagePortrait;
	private String leftIris;
	private String rightIris;
	private String signature;
	private FingerPrintDto primaryFingerPrint;
	private FingerPrintDto secondaryFingerPrint;
	
}
