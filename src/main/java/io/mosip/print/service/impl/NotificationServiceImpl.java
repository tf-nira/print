package io.mosip.print.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.print.constant.ApiName;
import io.mosip.print.constant.LoggerFileConstant;
import io.mosip.print.core.http.RequestWrapper;
import io.mosip.print.core.http.ResponseWrapper;
import io.mosip.print.dto.EmailResponseDTO;
import io.mosip.print.dto.ErrorDTO;
import io.mosip.print.dto.SmsRequestDTO;
import io.mosip.print.dto.SmsResponseDTO;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.service.NotificationService;
import io.mosip.print.service.PrintRestClientService;
import io.mosip.print.util.JsonUtil;
import io.mosip.print.util.RestApiClient;
import io.mosip.print.util.TemplateGenerator;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class NotificationServiceImpl implements NotificationService {

    Logger printLogger = PrintLogger.getLogger(NotificationServiceImpl.class);

    private static final String SMS_SERVICE_ID = "mosip.print.service.sms.id";
    public static final String PRINT_APPLICATION_VERSION = "print.service.version";
    public static final String DATETIME_PATTERN = "mosip.print.datetime.pattern";

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private TemplateGenerator templateGenerator;

    @Autowired
    private Environment env;

    @Autowired
    private RestApiClient restApiClient;

    @Autowired
    private PrintRestClientService<Object> restClientService;

    @Override
    public EmailResponseDTO sendEmail(String templateTypCode, String subjectCode, Map<String, Object> attributes, String email) throws Exception {
        EmailResponseDTO responseDto;

        try {
            String artifact = "";
            String subject = "";
            String lang = "eng";
            Map<String, Object> attributesLang=new HashMap<>(attributes);

            InputStream stream = templateGenerator.getTemplate(templateTypCode, attributesLang, lang);
            artifact = IOUtils.toString(stream, "UTF-8");

            InputStream subStream = templateGenerator.getTemplate(subjectCode, attributesLang, lang);
            subject = IOUtils.toString(subStream, "UTF-8");

            String[] mailTo = {email};

            LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            ResponseWrapper<?> responseWrapper;

            String apiHost = Objects.requireNonNull(env.getProperty(ApiName.EMAILNOTIFIER.name()));
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiHost);

            for (String item : mailTo) {
                params.add("mailTo", item);
            }

            params.add("mailSubject", subject);
            params.add("mailContent", artifact);

            printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID, "",
                    "NotificationServiceImpl::sendEmail():: EMAILNOTIFIER POST service started");

            responseWrapper = (ResponseWrapper<?>) restApiClient.postApi(builder.build().toUriString(),
                    MediaType.MULTIPART_FORM_DATA, params, ResponseWrapper.class);
            
            if (!responseWrapper.getErrors().isEmpty()) {
            	List<ErrorDTO> error = responseWrapper.getErrors();
				printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"NotificationServiceImpl::sendEmail():: error with error message "
								+ error.get(0).getMessage());
				throw new Exception(error.get(0).getMessage());
            }

            responseDto = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()), EmailResponseDTO.class);
            printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID, "",
                    "NotificationServiceImpl::sendEmail():: EMAILNOTIFIER POST service ended with in response : "
                            + JsonUtil.objectMapperObjectToJson(responseDto));

        } catch (Exception e) {
            throw new Exception(e);
        }

        return responseDto;
    }

    @Override
    public SmsResponseDTO sendSMS(String templateTypCode, Map<String, Object> attributes, String phone) throws Exception {
        SmsResponseDTO responseDto;

        try {
            String artifact = "";
            String lang = "eng";

            Map<String, Object> attributesLang=new HashMap<>(attributes);
            InputStream stream = templateGenerator.getTemplate(templateTypCode, attributesLang, lang);

            artifact = IOUtils.toString(stream, "UTF-8");

            SmsRequestDTO smsRequestDTO = new SmsRequestDTO();
            smsRequestDTO.setNumber(phone);
            smsRequestDTO.setMessage(artifact);

            String pattern = Objects.requireNonNull(env.getProperty(DATETIME_PATTERN));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

            String utcDateTimeStr = io.mosip.kernel.core.util.DateUtils.getUTCCurrentDateTimeString(pattern);
            LocalDateTime requestTime = LocalDateTime.parse(utcDateTimeStr, formatter);

            RequestWrapper<SmsRequestDTO> requestWrapper = new RequestWrapper<>();
            requestWrapper.setId(env.getProperty(SMS_SERVICE_ID));
            requestWrapper.setVersion(env.getProperty(PRINT_APPLICATION_VERSION));
            requestWrapper.setRequesttime(requestTime);
            requestWrapper.setRequest(smsRequestDTO);

            printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
                    "NotificationServiceImpl::sendSMS():: SMSNOTIFIER POST service started with request : "
                            + requestWrapper);

            ResponseWrapper<?>responseWrapper = (ResponseWrapper<?>) restClientService.postApi(ApiName.SMSNOTIFIER, "", "",
                    requestWrapper, ResponseWrapper.class);

            if (!responseWrapper.getErrors().isEmpty()) {
            	List<ErrorDTO> error = responseWrapper.getErrors();
				printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"NotificationServiceImpl::sendSMS():: error with error message "
								+ error.get(0).getMessage());
				throw new Exception(error.get(0).getMessage());
            }
            
            responseDto = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()), SmsResponseDTO.class);

            printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
                    "NotificationServiceImpl::sendSMS():: SMSNOTIFIER POST service ended with response : "
                            + JsonUtil.objectMapperObjectToJson(responseDto));
        } catch (Exception e) {
            throw new Exception(e);
        }

        return responseDto;
    }
}
