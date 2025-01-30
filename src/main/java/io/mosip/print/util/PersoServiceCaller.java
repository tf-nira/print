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
	
	public String callPersoService(PersoRequestDto request) {		
		HttpHeaders headers = new HttpHeaders();
		headers.set("ApiKey", apiKey);
		headers.set("SecretKey", secretKey);
		HttpEntity<Object> entity = new HttpEntity<>(request, headers);
		try {
		ResponseEntity<String> response = restTemplate.exchange(persoServiceUrl, HttpMethod.POST, entity, String.class);
		return response.getBody();
		}catch (Exception e) {
			logger.error("Error occurred while calling the perso service " + e.getMessage());
			return "failure";
		}
	}
}
