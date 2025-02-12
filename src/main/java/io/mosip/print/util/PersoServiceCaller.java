package io.mosip.print.util;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.print.dto.PersoRequestDto;
import io.mosip.print.logger.PrintLogger;

@Service
public class PersoServiceCaller {

	private Logger logger = PrintLogger.getLogger(PersoServiceCaller.class);
	
	@Value("${mosip.print.perso.apiKey}")
	private String apiKey;
	
	@Value("${mosip.print.perso.secretKey}")
	private String secretKey;
	
	@Value("${mosip.print.perso.api}")
	private String persoServiceUrl;
	
	@Autowired
	private RestTemplate restTemplate;
	
	private String requestString = "{\r\n"
			+ "	\"transactionId\": \"12345\",\r\n"
			+ "	\"externalRequestId\": \"2345656\",\r\n"
			+ "	\"cardNumber\": \"123456789\",\r\n"
			+ "	\"surName\": \"Partha\",\r\n"
			+ "	\"otherName\": \"Jemmy\",\r\n"
			+ "	\"givenName\": \"Ganish\",\r\n"
			+ "	\"nationality\": \"UGA\",\r\n"
			+ "	\"nationalityCode\": \"UGA\",\r\n"
			+ "	\"address\": {\r\n"
			+ "		\"village\": \"MATOVU 'B'(3)\",\r\n"
			+ "		\"parish\": \"MATOVU(32)\",\r\n"
			+ "		\"subCounty\": \"NANKOMA(4)\",\r\n"
			+ "		\"county\": \"BUKOOLI(099)\",\r\n"
			+ "		\"district\": \"BUGIRI (41)(BUGIRI (41))\"\r\n"
			+ "	},\r\n"
			+ "	\"nin\": \"12345\",\r\n"
			+ "	\"dateOfBirth\": \"01/01/1999\",\r\n"
			+ "	\"dateOfIssuance\": \"05/12/2024\",\r\n"
			+ "	\"dateOfExpiry\": \"05/12/2034\",\r\n"
			+ "	\"sexCode\": \"M\",\r\n"
			+ "	\"issuingCountryCode\": \"UGA\",\r\n"
			+ "	\"biometrics\": {\r\n"
			+ "		\"faceImagePortrait\": \"AAAADGpQICANCocKAAAAFGZ0eX\",\r\n"
			+ "		\"leftIris\": \"AAAADGpQICANCocKAAAAFGZ0eXBqcDIgAAAAAGpwMiAAAAA\",\r\n"
			+ "		\"rightIris\": \"AAAADGpQICANCocKAAAAFGZ0eXBqcDIgAAAAAGpwMiAAAAAt\",\r\n"
			+ "		\"signature\": \"AAAADGpQICANCocKAAAAFGZ0eXBqcDIgAAAAAGpwMiAAAAAtanAyaAAAABZpa\",\r\n"
			+ "		\"primaryFingerPrint\": {\r\n"
			+ "			\"image\": \"AAAADGpQICANCoc\",\r\n"
			+ "			\"index\": 7\r\n"
			+ "		},\r\n"
			+ "		\"secondaryFingerPrint\": {\r\n"
			+ "			\"image\": \"AAAADGpQICANCocKAAAAFGZ0\",\r\n"
			+ "			\"index\": 1\r\n"
			+ "		}\r\n"
			+ "	}\r\n"
			+ "}";	
	public String callPersoService(PersoRequestDto request) {
		logger.info("Calling perso service........");
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("ApiKey", apiKey);
		headers.set("SecretKey", secretKey);
		HttpEntity<Object> entity = new HttpEntity<>(request, headers);
		try {
		ResponseEntity<String> response = restTemplate.exchange(persoServiceUrl, HttpMethod.POST, entity, String.class);
		logger.info("Calling perso service response ........" + response.getBody());
		return response.getBody();
		}catch (Exception e) {
			logger.error("Error occurred while calling the perso service " + e.getMessage());
			return "failure";
		}
	}
	
	private PersoRequestDto getPersoRequest() {
		PersoRequestDto request = new PersoRequestDto();
		ObjectMapper mapper = new ObjectMapper();
		try {
			request = mapper.readValue(requestString, PersoRequestDto.class);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return request;
	}
}
