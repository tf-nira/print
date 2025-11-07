package io.mosip.print.service;

import io.mosip.print.dto.EmailResponseDTO;
import io.mosip.print.dto.SmsResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface NotificationService {

    EmailResponseDTO sendEmail(String templateTypCode, String subjectCode, Map<String, Object> attributes, String email) throws Exception;

    SmsResponseDTO sendSMS(String templateTypCode, Map<String, Object> attributes, String phone) throws Exception;
}
