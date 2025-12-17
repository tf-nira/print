package io.mosip.print.service.impl;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;

import com.google.gson.JsonArray;
import io.mosip.print.constant.*;
import io.mosip.print.dto.*;
import io.mosip.print.entity.NotificationStatus;
import io.mosip.print.exception.*;
import io.mosip.print.repository.NotificationStatusRepository;
import io.mosip.print.service.NotificationService;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.print.core.http.RequestWrapper;
import io.mosip.print.core.http.ResponseWrapper;
import io.mosip.print.dao.CardDetailDao;
import io.mosip.print.entity.CardDetail;
import io.mosip.print.logger.LogDescription;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.model.CredentialStatusEvent;
import io.mosip.print.model.EventModel;
import io.mosip.print.model.StatusEvent;
import io.mosip.print.repository.CardDetailRepository;
import io.mosip.print.service.PrintRestClientService;
import io.mosip.print.service.PrintService;
import io.mosip.print.service.UinCardGenerator;
import io.mosip.print.spi.CbeffUtil;
import io.mosip.print.spi.QrCodeGenerator;
import io.mosip.print.util.AuditLogRequestBuilder;
import io.mosip.print.util.CbeffToBiometricUtil;
import io.mosip.print.util.CryptoCoreUtil;
import io.mosip.print.util.CryptoUtil;
import io.mosip.print.util.DataShareUtil;
import io.mosip.print.util.DataUtil;
import io.mosip.print.util.DateUtils;
import io.mosip.print.util.JsonUtil;
import io.mosip.print.util.PersoServiceCaller;
import io.mosip.print.util.RestApiClient;
import io.mosip.print.util.TemplateGenerator;
import io.mosip.print.util.Utilities;
import io.mosip.print.util.WebSubSubscriptionHelper;

@Service
public class PrintServiceImpl implements PrintService{

	private String topic="CREDENTIAL_STATUS_UPDATE";

	private static final String SOURCE = "";
	
	@Autowired
	private WebSubSubscriptionHelper webSubSubscriptionHelper;

	@Autowired
	private DataShareUtil dataShareUtil;

	@Autowired
	CryptoUtil cryptoUtil;

	@Autowired
	private RestApiClient restApiClient;

	@Autowired
	private CryptoCoreUtil cryptoCoreUtil;

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = File.separator;

	/** The Constant VALUE. */
	private static final String VALUE = "value";

	/** The Constant UIN_CARD_TEMPLATE. */
	private static final String UIN_CARD_TEMPLATE = "RPR_UIN_CARD_TEMPLATE";

	/** The Constant MASKED_UIN_CARD_TEMPLATE. */
	private static final String MASKED_UIN_CARD_TEMPLATE = "RPR_MASKED_UIN_CARD_TEMPLATE";

	/** The Constant FACE. */
	private static final String FACE = "Face";

	/** The Constant UIN_CARD_PDF. */
	private static final String UIN_CARD_PDF = "uinPdf";

	/** The Constant UIN_TEXT_FILE. */
	private static final String UIN_TEXT_FILE = "textFile";

	/** The Constant APPLICANT_PHOTO. */
	private static final String APPLICANT_PHOTO = "ApplicantPhoto";

	/** The Constant QRCODE. */
	private static final String QRCODE = "QrCode";

	/** The Constant UINCARDPASSWORD. */
	private static final String UINCARDPASSWORD = "mosip.registration.processor.print.service.uincard.password";

	/** The print logger. */
	Logger printLogger = PrintLogger.getLogger(PrintServiceImpl.class);

//	/** The core audit request builder. */
//	@Autowired
//	private AuditLogRequestBuilder auditLogRequestBuilder;

	/** The template generator. */
	@Autowired
	private TemplateGenerator templateGenerator;

	/** The utilities. */
	@Autowired
	private Utilities utilities;

	/** The uin card generator. */
	@Autowired
	private UinCardGenerator<byte[]> uinCardGenerator;

	/** The rest client service. */
	@Autowired
	private PrintRestClientService<Object> restClientService;


	/** The qr code generator. */
	@Autowired
	private QrCodeGenerator<QrVersion> qrCodeGenerator;

	/** The Constant INDIVIDUAL_BIOMETRICS. */
	private static final String INDIVIDUAL_BIOMETRICS = "individualBiometrics";

	/** The Constant VID_CREATE_ID. */
	public static final String VID_CREATE_ID = "registration.processor.id.repo.generate";

	/** The Constant REG_PROC_APPLICATION_VERSION. */
	public static final String REG_PROC_APPLICATION_VERSION = "registration.processor.id.repo.vidVersion";

	/** The Constant DATETIME_PATTERN. */
	public static final String DATETIME_PATTERN = "mosip.print.datetime.pattern";

	private static final String NAME = "name";

	public static final String VID_TYPE = "registration.processor.id.repo.vidType";

	/** The cbeffutil. */
	@Autowired
	private CbeffUtil cbeffutil;

	/** The env. */
	@Autowired
	private Environment env;

	@Autowired
	private PublisherClient<String, Object, HttpHeaders> pb;
	
	@Autowired
	CardDetailRepository cardDetailRepository;
	
	@Value("${mosip.datashare.partner.id}")
	private String partnerId;

	@Value("${mosip.datashare.policy.id}")
	private String policyId;

	@Value("${mosip.template-language}")
	private String templateLang;

	@Value("${mosip.print.signature.filename:signature.png}")
	private String signatureFile;
	
	@Value("${print.service.send.data.fetchsize:5}")
	private Integer fetchSize;
	
	@Value("${print.service.send.notification.fetchsize:5}")
	private Integer notificationFetchSize;
	
	@Value("${print.service.send.data.threads.count:10}")
	private Integer numberOfThreads;
	
	@Value("${print.service.demo.match.required:true}")
	private Boolean isDemoMatchRequired;
	
	@Value("#{T(java.util.Arrays).asList('${print.service.legacy.check.process-names:RENEWAL,MIGRATOR}')}")
	private List<String> legacyCheckProcess;

	@Value("${mosip.print.service.delivered.email.code}")
	private String deliveredEmailCode;

	@Value("${mosip.print.service.delivered.email.subject.code}")
	private String deliveredEmailSubjectCode;

	@Value("${mosip.print.service.delivered.sms.code}")
	private String deliveredSmsCode;

	@Value("${mosip.print.service.ready.email.code}")
	private String readyEmailCode;

	@Value("${mosip.print.service.ready.email.subject.code}")
	private String readyEmailSubjectCode;

	@Value("${mosip.print.service.ready.sms.code}")
	private String readySmsCode;

	private static final String supportedLang = "eng";

	private ExecutorService executorService;
	
	@Autowired
	private PersoServiceCaller serviceCaller;
	
	@Autowired
	private CardDetailDao cardDetailDao;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationStatusRepository notificationStatusRepository;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(numberOfThreads); 
    }

	@Scheduled(cron = "${print.service.send.data.cron:0 0/3 * * * ?}")
	public void sendRecords() {
		printLogger.info("Starting batch job for sending requests");
		List<CardDetail> requests = cardDetailDao.fetchUnsentRecords(fetchSize);
		
		printLogger.info("Picked records to send: " + requests.size());
		requests.stream().map(request -> CompletableFuture
				.runAsync(() -> processSingleRequest(request), executorService).exceptionally(ex -> {
					printLogger.error("Failed to process request asynchronously: " + ex.getMessage(), ex);
					return null; 
				})).collect(Collectors.toList());
	}
	
	@Scheduled(cron = "${print.service.send.notification.cron:0 0/3 * * * ?}")
	public void sendNotifications() {
		printLogger.info("Starting batch job for sending notifications");
		List<NotificationStatus> requests = cardDetailDao.fetchUnnotifiedRecords(notificationFetchSize);
		
		printLogger.info("Picked records to send notifications: " + requests.size());
		requests.stream().map(request -> CompletableFuture
				.runAsync(() -> {
					Map<String, Object> attributes = new HashMap<>();
					if (request.getAttributes() != null) {
						try {
							attributes = mapper.readValue(request.getAttributes(), Map.class);
						} catch (JsonProcessingException e) {
							printLogger.error("Failed parsing attributes");
						}
					}
					sendNotification(request.getNin(), request.getTopic(), attributes, request);
				}, executorService).exceptionally(ex -> {
					printLogger.error("Failed to send notification asynchronously: " + ex.getMessage(), ex);
					return null; 
				})).collect(Collectors.toList());
	}
	
	private Object processSingleRequest(CardDetail request) {
		try {
			ObjectMapper objMapper = new ObjectMapper();
			EventModel eventModel = objMapper.readValue(request.getEventData(), EventModel.class);

			String decodedCrdential = null;
			String credential = null;

			if (eventModel.getEvent().getDataShareUri() == null || eventModel.getEvent().getDataShareUri().isEmpty()) {
				credential = eventModel.getEvent().getData().get("credential").toString();
			} else {
				String dataShareUrl = eventModel.getEvent().getDataShareUri();
				URI dataShareUri = URI.create(dataShareUrl);
				credential = restApiClient.getApi(dataShareUri, String.class);
			}
			String ecryptionPin = eventModel.getEvent().getData().get("protectionKey").toString();
			decodedCrdential = cryptoCoreUtil.decrypt(credential);
			Map proofMap = new HashMap<String, String>();
			proofMap = (Map) eventModel.getEvent().getData().get("proof");
			String sign = proofMap.get("signature").toString();
			String registrationId = (String) eventModel.getEvent().getData().get("registrationId");
			PersoRequestDto persoRequestDto = getPersoRequest(decodedCrdential,
					eventModel.getEvent().getData().get("credentialType").toString(), ecryptionPin,
					eventModel.getEvent().getTransactionId(), sign, "UIN", false, null, registrationId, true);

			String response = serviceCaller.callPersoService(persoRequestDto);

			if (response != null && !response.trim().equalsIgnoreCase("failure")) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode rootNode = mapper.readTree(response);
					boolean isSuccess = rootNode.path("isSuccess").asBoolean(false);

					if (isSuccess) {
						printLogger.info("Request sent for transaction id: " + request.getTransactionId());
						request.setIsProcessing(false);
						request.setIsPushed(true);
						request.setUpdatedBy("SYSTEM");
						request.setUpdatedTimes(LocalDateTime.now());
						cardDetailRepository.save(request);
					} else {
						String errorMessage = "";

					    try {
					        JsonNode errorsNode = rootNode.path("errors");
					        if (errorsNode.isArray() && errorsNode.size() > 0) {
					            JsonNode firstError = errorsNode.get(0);
					            errorMessage = firstError.path("message").asText("");
					            printLogger.info("Failed sending request for transaction id: {}, error: {}", request.getTransactionId(), errorMessage);
					        }
					    } catch (Exception e) {
					        printLogger.error("Error while extracting error message from response: " + e.getMessage(), e);
					    }
					    
					    request.setIsProcessing(false);
						request.setIsFailed(true);
						request.setRemark(errorMessage);
						request.setUpdatedBy("SYSTEM");
						request.setUpdatedTimes(LocalDateTime.now());
						cardDetailRepository.save(request);
					}
				} catch (Exception e) {
					printLogger.error("Failed to parse perso service response: " + e.getMessage(), e);
				}
			} else {
				request.setIsProcessing(false);
				request.setIsFailed(true);
				request.setRemark("Error while sending to perso");
				request.setUpdatedBy("SYSTEM");
				request.setUpdatedTimes(LocalDateTime.now());
				cardDetailRepository.save(request);
			}
		} catch (Exception e) {
			printLogger.error("Failed to send request: " + e.getMessage(), e);
			request.setIsProcessing(false);
			request.setIsFailed(true);
			request.setRemark(e.getMessage());
			request.setUpdatedBy("SYSTEM");
			request.setUpdatedTimes(LocalDateTime.now());
			cardDetailRepository.save(request);
		}
		return true;
	}

	public boolean generateCard(EventModel eventModel) {	

		String decodedCrdential = null;
		String credential = null;
		boolean isPrinted=false;
		try {
			//printLogger.info("supportedLang ++++++++++" ,supportedLang);
			if (eventModel.getEvent().getDataShareUri() == null || eventModel.getEvent().getDataShareUri().isEmpty()) {
				credential = eventModel.getEvent().getData().get("credential").toString();
			} else {
				String dataShareUrl = eventModel.getEvent().getDataShareUri();
				URI dataShareUri = URI.create(dataShareUrl);
				credential = restApiClient.getApi(dataShareUri, String.class);
			}
			String ecryptionPin = eventModel.getEvent().getData().get("protectionKey").toString();
			decodedCrdential = cryptoCoreUtil.decrypt(credential);
			Map proofMap = new HashMap<String, String>();
			proofMap = (Map) eventModel.getEvent().getData().get("proof");
			String sign = proofMap.get("signature").toString();
			String registrationId = (String) eventModel.getEvent().getData().get("registrationId");
			PersoRequestDto persoRequestDto = getPersoRequest(decodedCrdential,
					eventModel.getEvent().getData().get("credentialType").toString(), ecryptionPin,
					eventModel.getEvent().getTransactionId(), sign, "UIN", false, eventModel, registrationId, false);
			//Need to uncomment once data correct confirmed
//			serviceCaller.callPersoService(persoRequestDto);	
		}catch (Exception e){
			printLogger.error(e.getMessage() , e);
			return isPrinted=false;
		}
		return isPrinted=true;
	}
/*
	private String getSignature(String sign, String crdential) {
		String signHeader = sign.split("\\.")[0];
		String signData = sign.split("\\.")[2];
		String signature = signHeader + "." + crdential + "." + signData;
		return signature;
	}
*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.print.service.PrintService#
	 * getDocuments(io.mosip.registration.processor.core.constant.IdType,
	 * java.lang.String, java.lang.String, boolean)
	 */

	@SuppressWarnings("rawtypes")
	private PersoRequestDto getPersoRequest(String credential, String credentialType, String encryptionPin,
			String requestId, String sign,
			String cardType,
			boolean isPasswordProtected, EventModel eventModel, String registrationId, boolean isBioExtractionRequired) {
		printLogger.debug("PrintServiceImpl::getDocuments()::entry");
		PersoRequestDto persoRequestDto=new PersoRequestDto();
		String credentialSubject;

		String uin = null;
		LogDescription description = new LogDescription();

		boolean isTransactionSuccessful = false;


		try {
			credentialSubject = getCrdentialSubject(credential);
			org.json.JSONObject credentialSubjectJson = new org.json.JSONObject(credentialSubject);
			org.json.JSONObject decryptedJson = decryptAttribute(credentialSubjectJson, encryptionPin, credential);			

			//printLogger.info("decryptedJson " + decryptedJson.toString());	
			
			//printLogger.info("attributes from set template " +attributes.toString());	
			persoRequestDto.setResidenceStatus(getAttribute(decryptedJson, "residenceStatus"));
			persoRequestDto.setApplicantForeignResidenceCountry(
					getAttribute(decryptedJson, "applicantForeignResidenceCountry"));
			PersoAddressDto persoAddressDto=new PersoAddressDto();
			persoAddressDto.setCounty(getAttribute(decryptedJson, "applicantPlaceOfResidenceCounty"));
			persoAddressDto.setDistrict(getAttribute(decryptedJson, "applicantPlaceOfResidenceDistrict"));
			persoAddressDto.setSubCounty(getAttribute(decryptedJson, "applicantPlaceOfResidenceSubCounty"));
			persoAddressDto.setParish(DataUtil
					.getParishOrVillageCorrectData(getAttribute(decryptedJson, "applicantPlaceOfResidenceParish")));
			persoAddressDto.setVillage(DataUtil
					.getParishOrVillageCorrectData(getAttribute(decryptedJson, "applicantPlaceOfResidenceVillage")));
			persoRequestDto.setAddress(persoAddressDto);

			PersoEnrollmenetAddressDTO persoEnrollmenetAddressDTO=new PersoEnrollmenetAddressDTO();
			persoEnrollmenetAddressDTO.setCounty(getAttribute(decryptedJson, "applicantPlaceOfEnrolmentCounty"));
			persoEnrollmenetAddressDTO.setDistrict(getAttribute(decryptedJson, "applicantPlaceOfEnrolmentDistrict"));
			persoEnrollmenetAddressDTO.setSubCounty(getAttribute(decryptedJson, "applicantPlaceOfEnrolmentSubCounty"));
			persoEnrollmenetAddressDTO.setParish(DataUtil.getParishOrVillageCorrectData(getAttribute(decryptedJson, "applicantPlaceOfEnrolmentParish")));
			persoEnrollmenetAddressDTO.setVillage(DataUtil
					.getParishOrVillageCorrectData(getAttribute(decryptedJson, "applicantPlaceOfEnrolmentVillage")));
			persoRequestDto.setPlaceOfEnrollment(persoEnrollmenetAddressDTO);

			persoRequestDto.setDateOfIssuance(getString(decryptedJson, "dateOfIssuance"));
			persoRequestDto.setDateOfExpiry(getString(decryptedJson, "dateOfExpiry"));
			persoRequestDto.setNationality(getString(decryptedJson, "Nationality"));
			String givenName = getAttribute(decryptedJson, "givenName");
			if (givenName != null) {
				persoRequestDto.setGivenName(givenName.toUpperCase());
			} else {
				persoRequestDto.setGivenName(givenName);
			}
			String otherNames = getAttribute(decryptedJson, "otherNames");
			if (otherNames != null) {
				persoRequestDto.setOtherName(otherNames.toUpperCase());
			} else {
				persoRequestDto.setOtherName(otherNames);
			}
			String surname = getAttribute(decryptedJson, "surname");
			if (surname != null) {
				persoRequestDto.setSurName(surname.toUpperCase());
			} else {
				persoRequestDto.setSurName(surname);
			}
			String gender = getAttribute(decryptedJson, "gender");
			persoRequestDto.setSexCode((gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("M")) ? "M" : "F");
			persoRequestDto.setDateOfBirth(getString(decryptedJson, "dateOfBirth"));
			persoRequestDto.setExternalRequestId(registrationId);
			persoRequestDto.setTransactionId(requestId);
			persoRequestDto.setNationalityCode("UGA");
			persoRequestDto.setIssuingCountryCode("UGA");
			String NIN = getString(decryptedJson, "NIN");
			if (NIN != null) {
				persoRequestDto.setNin(NIN.toUpperCase());
			} else {
				persoRequestDto.setNin(NIN);
			}

			PersoBiometricsDto persoBiometricsDto=new PersoBiometricsDto();
			String faceCbeff = getString(decryptedJson, "Face");
			if (faceCbeff != null) {
				persoBiometricsDto.setFaceImagePortrait(getExtractedBiometrics(faceCbeff, "Face", null, true, isBioExtractionRequired));
			} else {
				persoBiometricsDto.setFaceImagePortrait(null);
			}
			String irisCbeff = getString(decryptedJson, "Iris");
			if (irisCbeff != null) {
				persoBiometricsDto.setLeftIris(getExtractedBiometrics(irisCbeff, "Iris", "Left", false, isBioExtractionRequired));
				persoBiometricsDto.setRightIris(getExtractedBiometrics(irisCbeff, "Iris", "Right", false, isBioExtractionRequired));
			} else {
				persoBiometricsDto.setLeftIris(null);
				persoBiometricsDto.setRightIris(null);
			}
			String signature = getString(decryptedJson, "signature");
			if (signature != null && signature.equalsIgnoreCase("Unable to Sign")) {
				InputStream in = getClass().getClassLoader().getResourceAsStream(signatureFile);
				byte[] signatureBytes = in.readAllBytes();
				persoBiometricsDto.setSignature(java.util.Base64.getEncoder().encodeToString(signatureBytes));
			} else {
				persoBiometricsDto.setSignature(signature);
			}

			String bestTwoFingers = getString(decryptedJson, "bestTwoFingers");
			if (bestTwoFingers != null) {
				JSONParser parser = new JSONParser();
				JSONArray jsonArray = (JSONArray) parser.parse(bestTwoFingers);
			
		    	 if(jsonArray.get(0)!=null) {
		    		 JSONObject jsonObject = (JSONObject) jsonArray.get(0);
						Long fingersIndex = (Long) jsonObject.get("fingersIndex");
		               String fingerPrint = (String) jsonObject.get("fingerPrint");
						String rawFinger = getExtractedBiometrics(fingerPrint, "Finger", null, false, isBioExtractionRequired);
						FingerPrintDto fingerPrintDto = new FingerPrintDto();
						if (rawFinger != null) {
							fingerPrintDto.setIndex(fingersIndex.intValue());
							fingerPrintDto.setImage(rawFinger);
						} else {
							fingerPrintDto.setIndex(null);
							fingerPrintDto.setImage(null);
						}
				 		if(!isBioExtractionRequired) {
							fingerPrintDto.setIndex(fingersIndex.intValue());
						}
						persoBiometricsDto.setPrimaryFingerPrint(fingerPrintDto);
				}
		    	 if(jsonArray.get(1)!=null) {
		    		 JSONObject jsonObject = (JSONObject) jsonArray.get(1);
						Long fingersIndex = (Long) jsonObject.get("fingersIndex");
						String fingerPrint = (String) jsonObject.get("fingerPrint");
						String rawFinger = getExtractedBiometrics(fingerPrint, "Finger", null, false, isBioExtractionRequired);
						FingerPrintDto fingerPrintDto = new FingerPrintDto();
						if (rawFinger != null) {
							fingerPrintDto.setIndex(fingersIndex.intValue());
							fingerPrintDto.setImage(rawFinger);
						} else {
							fingerPrintDto.setIndex(null);
							fingerPrintDto.setImage(null);
						}
				 		if(!isBioExtractionRequired) {
							fingerPrintDto.setIndex(fingersIndex.intValue());
						}
						persoBiometricsDto.setSecondaryFingerPrint(fingerPrintDto);
				}
			} else {
				FingerPrintDto fingerPrintDto = new FingerPrintDto();
				fingerPrintDto.setIndex(null);
				fingerPrintDto.setImage(null);
				persoBiometricsDto.setPrimaryFingerPrint(fingerPrintDto);
				persoBiometricsDto.setSecondaryFingerPrint(fingerPrintDto);
			}
			
				persoRequestDto.setBiometrics(persoBiometricsDto);

			isTransactionSuccessful = true;

		}  catch (Exception ex) {
			description.setMessage(PlatformErrorMessages.PRT_PRT_PDF_GENERATION_FAILED.getMessage());
			description.setCode(PlatformErrorMessages.PRT_PRT_PDF_GENERATION_FAILED.getCode());
			printLogger.error(ex.getMessage(), ex);
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					ex.getMessage(), ex);

		} finally {
//			try {
//				printLogger.info("Object mapper PersoRequestDto in finally  " + new ObjectMapper().writeValueAsString(persoRequestDto));
//			} catch (JsonProcessingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			if (eventModel != null) {
				try {
					Optional<CardDetail> existingRecordOpt = cardDetailRepository.findByNinAndRegId(persoRequestDto.getNin(), registrationId);
					
					if (existingRecordOpt.isPresent()) {
						printLogger.info("Card detail already exists for NIN and registrationId. Skipping insert.");
					} else {
						printLogger.info("Saving new card details");
						CardDetail cardDetail = new CardDetail();
						populateCardDetail(cardDetail, persoRequestDto, registrationId, eventModel);
						cardDetail.setCreatedBy("SYSTEM");
						cardDetail.setCrDTimes(LocalDateTime.now());
						cardDetailRepository.save(cardDetail);
						printLogger.info("Card details saved");
					}
				} catch (Exception e) {
					printLogger.error("Error while saving data: ", e);
				}
			}
			
			//printLogger.info("persoRequestDto in finally " + persoRequestDto.toString());
			String eventId = "";
			String eventName = "";
			String eventType = "";
			if (isTransactionSuccessful) {
				description.setMessage(PlatformSuccessMessages.RPR_PRINT_SERVICE_SUCCESS.getMessage());
				description.setCode(PlatformSuccessMessages.RPR_PRINT_SERVICE_SUCCESS.getCode());

				eventId = EventId.RPR_402.toString();
				eventName = EventName.UPDATE.toString();
				eventType = EventType.BUSINESS.toString();
			} else {
				description.setMessage(PlatformErrorMessages.PRT_PRT_PDF_GENERATION_FAILED.getMessage());
				description.setCode(PlatformErrorMessages.PRT_PRT_PDF_GENERATION_FAILED.getCode());

				eventId = EventId.RPR_405.toString();
				eventName = EventName.EXCEPTION.toString();
				eventType = EventType.SYSTEM.toString();
			}
			/** Module-Id can be Both Success/Error code */
			String moduleId = isTransactionSuccessful ? PlatformSuccessMessages.RPR_PRINT_SERVICE_SUCCESS.getCode()
					: description.getCode();
			String moduleName = ModuleName.PRINT_SERVICE.toString();
//			auditLogRequestBuilder.createAuditRequestBuilder(description.getMessage(), eventId, eventName, eventType,
//					moduleId, moduleName, uin);
		}
		printLogger.debug("PrintServiceImpl::getDocuments()::exit");

	
		return persoRequestDto;
	}
	
	private void populateCardDetail(CardDetail cardDetail, PersoRequestDto persoRequestDto, String registrationId, EventModel eventModel) throws JsonProcessingException {
		cardDetail.setTransactionId(persoRequestDto.getTransactionId());
		cardDetail.setRegId(registrationId);
		cardDetail.setNin(persoRequestDto.getNin());
		cardDetail.setGivenName(persoRequestDto.getGivenName());
		cardDetail.setSurname(persoRequestDto.getSurName());
		cardDetail.setOtherName(persoRequestDto.getOtherName());
		cardDetail.setNationality(persoRequestDto.getNationality());
		cardDetail.setSex(persoRequestDto.getSexCode());
		cardDetail.setDateOfBirth(persoRequestDto.getDateOfBirth());
		String prFingerName = FingerType
				.getNameByIndex(persoRequestDto.getBiometrics().getPrimaryFingerPrint() != null
						? persoRequestDto.getBiometrics().getPrimaryFingerPrint().getIndex()
						: null);
		String secFingerName = FingerType
				.getNameByIndex(persoRequestDto.getBiometrics().getSecondaryFingerPrint() != null
						? persoRequestDto.getBiometrics().getSecondaryFingerPrint().getIndex()
						: null);
		cardDetail.setPrimaryFinger(prFingerName);
		cardDetail.setSecondaryFinger(secFingerName);
		cardDetail.setDateOfIssue(persoRequestDto.getDateOfIssuance());
		cardDetail.setDateOfExpiry(persoRequestDto.getDateOfExpiry());
		cardDetail.setEventData(new ObjectMapper().writeValueAsString(eventModel));
		String process = (String) eventModel.getEvent().getData().get("registrationType");
		//Need to change to true once data correct confirmed
		cardDetail.setIsReadyToPush(isReadyToPush(cardDetail, process));
		cardDetail.setIsPushed(false);
		cardDetail.setIsFailed(false);
	}
	
	private boolean isReadyToPush(CardDetail cardDetail, String process) {
		if (!isDemoMatchRequired || (process != null && !legacyCheckProcess.contains(process))) {
			return true;
		}
		
		boolean isReadyToPush = false;
		
		try {
			printLogger.info("Calling migration api for demographic match");
			RequestWrapper<NinDetailsRequest> requestWrapper = new RequestWrapper<>();
			NinDetailsRequest request = new NinDetailsRequest();
			request.setNin(cardDetail.getNin());
			requestWrapper.setRequest(request);
			
			ResponseWrapper<?> responseWrapper;
			NinDetailsResponse ninDetailsResponse;
			
			responseWrapper = (ResponseWrapper<?>)restClientService.postApi(ApiName.MIGRATIONUTILITYURL, null, null,
					requestWrapper, ResponseWrapper.class, MediaType.APPLICATION_JSON);
			
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				ErrorDTO error = responseWrapper.getErrors().get(0);
			    
				printLogger.error("Error from migration api: " + error.getMessage());
				
				//New case
				if ("Error : No data found for given nin".equalsIgnoreCase(error.getMessage())) {
					return true;
				} else {
					cardDetail.setRemark("Error from migration api: " + error.getMessage());
					return false;
				}
			}
			
			if (responseWrapper.getResponse() != null) {
				ninDetailsResponse = mapper.convertValue(responseWrapper.getResponse(), NinDetailsResponse.class);
				
				printLogger.info("Received response from migration api");
				
				if (ninDetailsResponse != null && ninDetailsResponse.getDemographics() != null) {
					isReadyToPush = isDemographicMatch(cardDetail, ninDetailsResponse.getDemographics());
					String regId = cardDetail.getRegId();
					
					if (isReadyToPush) {
						if (regId != null && regId.length() == 13) {
							isReadyToPush = false;
							cardDetail.setRemark("Face check");
						}
					} else {
						cardDetail.setRemark("Incorrect demographics");
						
						if (regId != null && regId.length() == 13) {
							cardDetail.setRemark("Incorrect demographics and Face check");
						}
					}
					
					printLogger.info("Demographic comparison done");
				}
			}
		} catch (Exception e) {
			printLogger.error("Error while calling migration api: " + e.getMessage());
			cardDetail.setRemark("Migration api call failed: " + e.getMessage());
		}
		
		return isReadyToPush;
	}
	
	private boolean isDemographicMatch(CardDetail cardDetail, DemographicDto demo) {
	    return Objects.equals(cardDetail.getNin(), demo.getNin())
	        && Objects.equals(cardDetail.getGivenName(), getFirstValue(demo.getGivenName()))
	        && Objects.equals(cardDetail.getSurname(), getFirstValue(demo.getSurname()))
	        && Objects.equals(cardDetail.getOtherName(), getFirstValue(demo.getOtherNames()))
	        && Objects.equals(cardDetail.getSex(), getFirstValue(demo.getGender()).equals("Male") ? "M" : "F")
	        && Objects.equals(cardDetail.getDateOfBirth(), demo.getDateOfBirth());
	}
	
	private String getFirstValue(String jsonArrayAsString) {
		if (jsonArrayAsString == null || jsonArrayAsString.trim().isEmpty()) {
	        return "";
	    }
		
	    try {
	        JsonValue[] values = mapper.readValue(jsonArrayAsString, JsonValue[].class);
	        if (values != null && values.length > 0 && values[0].getValue() != null) {
	            return values[0].getValue().trim();
	        }
	    } catch (Exception e) {
	        printLogger.error("Failed to parse JsonValue array: {}", e.getMessage());
	    }
	    return "";
	}

	private String getAttribute(org.json.JSONObject  json, String attr) throws ParseException {
		if (json.has(attr)) {
		Object obj=json.get(attr);
		if(obj!=null) {
		JSONParser parser = new JSONParser();
    	JSONArray jsonArray = (JSONArray) parser.parse(obj.toString());
    	if(jsonArray.get(0)!=null) {
   		 JSONObject jsonObject = (JSONObject) jsonArray.get(0);
   		  return ((String) jsonObject.get("value")).trim();
		
		  }
    	 }
		}
		return "";
	}

	private String getString(org.json.JSONObject json, String attr) throws ParseException {
		String value = null;
		if (json.has(attr)) {
			Object obj = json.get(attr);
			if (obj != null) {
				value = obj.toString().trim();
			}
		}
		return value;
	}

	/**
	 * Creates the text file.
	 *
	 * @param jsonString
	 *            the attributes
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private String createTextFile(String jsonString) throws IOException {
		LinkedHashMap<String, String> printTextFileMap = new LinkedHashMap<>();
		JSONObject demographicIdentity = JsonUtil.objectMapperReadValue(jsonString, JSONObject.class);
		if (demographicIdentity == null)
			throw new IdentityNotFoundException(PlatformErrorMessages.PRT_PIS_IDENTITY_NOT_FOUND.getMessage());
		String printTextFileJson = utilities.getPrintTextFileJson(utilities.getConfigServerFileStorageURL(),
				utilities.getRegistrationProcessorPrintTextFile());
		JSONObject printTextFileJsonObject = JsonUtil.objectMapperReadValue(printTextFileJson, JSONObject.class);
		Set<String> printTextFileJsonKeys = printTextFileJsonObject.keySet();
		for (String key : printTextFileJsonKeys) {
			String printTextFileJsonString = JsonUtil.getJSONValue(printTextFileJsonObject, key);
			for (String value : printTextFileJsonString.split(",")) {
				Object object = demographicIdentity.get(value);
				if (object instanceof ArrayList) {
					JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
					JsonValue[] jsonValues = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, node);
					for (JsonValue jsonValue : jsonValues) {
						if (supportedLang.contains(jsonValue.getLanguage()))
							printTextFileMap.put(value + "_" + jsonValue.getLanguage(), jsonValue.getValue());
					}
				} else if (object instanceof LinkedHashMap) {
					JSONObject json = JsonUtil.getJSONObject(demographicIdentity, value);
					printTextFileMap.put(value, (String) json.get(VALUE));
				} else {
					printTextFileMap.put(value, (String) object);

				}
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		return gson.toJson(printTextFileMap);
	}

	/**
	 * Sets the qr code.
	 *
	 * @param attributes   the attributes
	 * @return true, if successful
	 * @throws QrcodeGenerationException                          the qrcode
	 *                                                            generation
	 *                                                            exception
	 * @throws IOException                                        Signals that an
	 *                                                            I/O exception has
	 *                                                            occurred.
	 * @throws io.mosip.print.exception.QrcodeGenerationException
	 */
	private boolean setQrCode(String qrString, Map<String, Object> attributes)
			throws QrcodeGenerationException, IOException, io.mosip.print.exception.QrcodeGenerationException {
		boolean isQRCodeSet = false;
		JSONObject qrJsonObj = JsonUtil.objectMapperReadValue(qrString, JSONObject.class);
		qrJsonObj.remove("biometrics");
		// String digitalSignaturedQrData =
		// digitalSignatureUtility.getDigitalSignature(qrString);
		// JSONObject textFileJson = new JSONObject();
		// textFileJson.put("digitalSignature", digitalSignaturedQrData);
		// Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		// String printTextFileString = gson.toJson(textFileJson);
		byte[] qrCodeBytes = qrCodeGenerator.generateQrCode(qrJsonObj.toString(), QrVersion.V30);
		if (qrCodeBytes != null) {
			String imageString = Base64.encodeBase64String(qrCodeBytes);
			attributes.put(QRCODE, "data:image/png;base64," + imageString);
			isQRCodeSet = true;
		}

		return isQRCodeSet;
	}

	/**
	 * Sets the applicant photo.
	 *
	 *            the response
	 * @param attributes
	 *            the attributes
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	private boolean setApplicantPhoto(String individualBio, Map<String, Object> attributes, boolean isFaceExtractionRequired) throws Exception {
		String value = individualBio;
		boolean isPhotoSet = false;

		if (value != null) {
			CbeffToBiometricUtil util = new CbeffToBiometricUtil(cbeffutil);
			List<String> subtype = new ArrayList<>();
			byte[] photoByte = util.getImageBytes(value, FACE, subtype);
			if (photoByte != null && isFaceExtractionRequired) {
				String data = java.util.Base64.getEncoder().encodeToString(extractFaceImageData(photoByte));
				attributes.put(APPLICANT_PHOTO, "data:image/png;base64," + data);
				isPhotoSet = true;
			}
		}
		return isPhotoSet;
	}
	
	private String getFaceBiometrics(String individualBio,String type,String subtype) throws Exception {
		String value = individualBio;
		String data=null;

		if (value != null) {
			CbeffToBiometricUtil util = new CbeffToBiometricUtil(cbeffutil);
			List<String> subtypeList = new ArrayList<>();
			if(subtype!=null) {
				subtypeList.add(subtype);
			}
			byte[] photoByte = util.getImageBytes(value, type, subtypeList);
			if (photoByte != null) {
				 data = java.util.Base64.getEncoder().encodeToString(extractFaceImageData(photoByte));
				
			}
		}
		return data;
	}

	private String getExtractedBiometrics(String individualBio, String type, String subType, boolean isUpscaleRequired, boolean isBioExtractionRequired)
			throws Exception {
		if(!isBioExtractionRequired) {
			return null;
		}
		String data=null;
		Map<String, String> bdbBasedOnFinger = cbeffutil.getBDBBasedOnType(Base64.decodeBase64(individualBio), type,
				subType);
		for (Entry<String, String> iterable_element : bdbBasedOnFinger.entrySet()) {
			if (iterable_element.getValue() != null) {
				byte[] fingerData = convertToJPG(iterable_element.getValue(), isUpscaleRequired);
				data = java.util.Base64.getEncoder().encodeToString(fingerData);
			}
		}

		return data;
	}


	/**
	 * Gets the artifacts.
	 *
	 * @param attribute    the attribute
	 * @return the artifacts
	 * @throws IOException    Signals that an I/O exception has occurred.
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	private void setTemplateAttributes(String jsonString, Map<String, Object> attribute)
			throws IOException, ParseException {
		try {
			JSONObject demographicIdentity = JsonUtil.objectMapperReadValue(jsonString, JSONObject.class);
			if (demographicIdentity == null)
				throw new IdentityNotFoundException(PlatformErrorMessages.PRT_PIS_IDENTITY_NOT_FOUND.getMessage());

			String mapperJsonString = utilities.getIdentityMappingJson(utilities.getConfigServerFileStorageURL(),
					utilities.getGetRegProcessorIdentityJson());
			JSONObject mapperJson = JsonUtil.objectMapperReadValue(mapperJsonString, JSONObject.class);
			JSONObject mapperIdentity = JsonUtil.getJSONObject(mapperJson,
					utilities.getGetRegProcessorDemographicIdentity());

			List<String> mapperJsonKeys = new ArrayList<>(mapperIdentity.keySet());
			for (String key : mapperJsonKeys) {
				LinkedHashMap<String, String> jsonObject = JsonUtil.getJSONValue(mapperIdentity, key);
				Object obj = null;
				String values = jsonObject.get(VALUE);
				for (String value : values.split(",")) {
					// Object object = demographicIdentity.get(value);
					Object object = demographicIdentity.get(value);
					if (object != null) {
						try {
						obj = new JSONParser().parse(object.toString());
						} catch (Exception e) {
							printLogger.error("Error while parsing Json field" ,e);
							obj = object;
						}
					
					if (obj instanceof JSONArray) {
						// JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
						JsonValue[] jsonValues = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, (JSONArray) obj);
						printLogger.error("JSONArray obj++++++++++" ,obj);
						printLogger.error("jsonValues ++++++++++" ,jsonValues);
						
						for (JsonValue jsonValue : jsonValues) {
							
							if (supportedLang.contains(jsonValue.getLanguage()))
								attribute.put(value + "_" + jsonValue.getLanguage(), jsonValue.getValue());


						}

					} else if (object instanceof JSONObject) {
						JSONObject json = (JSONObject) object;
						attribute.put(value, (String) json.get(VALUE));
					} else {
						attribute.put(value, String.valueOf(object));
					}
				}
					
				}
			}

		} catch (JsonParseException | JsonMappingException e) {
			printLogger.error("Error while parsing Json file" ,e);
			throw new ParsingException(PlatformErrorMessages.PRT_RGS_JSON_PARSING_EXCEPTION.getMessage(), e);
		}
	}

	/**
	 * Mask string.
	 *
	 * @param uin
	 *            the uin
	 * @param maskLength
	 *            the mask length
	 * @param maskChar
	 *            the mask char
	 * @return the string
	 */
	private String maskString(String uin, int maskLength, char maskChar) {
		if (uin == null || "".equals(uin))
			return "";

		if (maskLength == 0)
			return uin;

		StringBuilder sbMaskString = new StringBuilder(maskLength);

		for (int i = 0; i < maskLength; i++) {
			sbMaskString.append(maskChar);
		}

		return sbMaskString.toString() + uin.substring(0 + maskLength);
	}

	/**
	 * Gets the vid.
	 *
	 * @param uin the uin
	 * @return the vid
	 * @throws ApisResourceAccessException the apis resource access exception
	 * @throws VidCreationException        the vid creation exception
	 * @throws IOException                 Signals that an I/O exception has
	 *                                     occurred.
	 */
	private String getVid(String uin) throws ApisResourceAccessException, VidCreationException, IOException {
		String vid;
		VidRequestDto vidRequestDto = new VidRequestDto();
		RequestWrapper<VidRequestDto> request = new RequestWrapper<>();
		VidResponseDTO vidResponse;
		vidRequestDto.setUIN(uin);
		vidRequestDto.setVidType(env.getProperty(VID_TYPE));
		request.setId(env.getProperty(VID_CREATE_ID));
		request.setRequest(vidRequestDto);
		DateTimeFormatter format = DateTimeFormatter.ofPattern(env.getProperty(DATETIME_PATTERN));
		LocalDateTime localdatetime = LocalDateTime
				.parse(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)), format);
		request.setRequesttime(localdatetime);
		request.setVersion(env.getProperty(REG_PROC_APPLICATION_VERSION));

		printLogger.debug("PrintServiceImpl::getVid():: post CREATEVID service call started with request data : "
						+ JsonUtil.objectMapperObjectToJson(vidRequestDto));

		vidResponse = (VidResponseDTO) restClientService.postApi(ApiName.CREATEVID, "", "", request,
				VidResponseDTO.class);

		printLogger.debug("PrintServiceImpl::getVid():: post CREATEVID service call ended successfully");

		if (vidResponse.getErrors() != null && !vidResponse.getErrors().isEmpty()) {
			throw new VidCreationException(PlatformErrorMessages.PRT_PRT_VID_EXCEPTION.getCode(),
					PlatformErrorMessages.PRT_PRT_VID_EXCEPTION.getMessage());

		} else {
			vid = vidResponse.getResponse().getVid();
		}

		return vid;
	}

	/**
	 * Gets the password.
	 *
	 * @param uin
	 *            the uin
	 * @return the password
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 * @throws NumberFormatException
	 *             the number format exception
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String getPassword(String uin) throws ApisResourceAccessException, IOException {
		JSONObject jsonObject = utilities.retrieveIdrepoJson(uin);

		String[] attributes = env.getProperty(UINCARDPASSWORD).split("\\|");
		List<String> list = new ArrayList<>(Arrays.asList(attributes));

		Iterator<String> it = list.iterator();
		String uinCardPd = "";

		while (it.hasNext()) {
			String key = it.next().trim();

			Object object = JsonUtil.getJSONValue(jsonObject, key);
			if (object instanceof ArrayList) {
				JSONArray node = JsonUtil.getJSONArray(jsonObject, key);
				JsonValue[] jsonValues = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, node);
				uinCardPd = uinCardPd.concat(getParameter(jsonValues, templateLang));

			} else if (object instanceof LinkedHashMap) {
				JSONObject json = JsonUtil.getJSONObject(jsonObject, key);
				uinCardPd = uinCardPd.concat((String) json.get(VALUE));
			} else {
				uinCardPd = uinCardPd.concat((String) object);
			}

		}

		return uinCardPd;
	}

	/**
	 * Gets the parameter.
	 *
	 * @param jsonValues
	 *            the json values
	 * @param langCode
	 *            the lang code
	 * @return the parameter
	 */
	private String getParameter(JsonValue[] jsonValues, String langCode) {

		String parameter = null;
		if (jsonValues != null) {
			for (int count = 0; count < jsonValues.length; count++) {
				String lang = jsonValues[count].getLanguage();
				if (langCode.contains(lang)) {
					parameter = jsonValues[count].getValue();
					break;
				}
			}
		}
		return parameter;
	}

	public byte[] extractFaceImageData(byte[] decodedBioValue) {		
		try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(decodedBioValue))) {

			byte[] format = new byte[4];
			din.read(format, 0, 4);
			byte[] version = new byte[4];
			din.read(version, 0, 4);
			int recordLength = din.readInt();
			short numberofRepresentionRecord = din.readShort();
			byte certificationFlag = din.readByte();
			byte[] temporalSequence = new byte[2];
			din.read(temporalSequence, 0, 2);
			int representationLength = din.readInt();
			byte[] representationData = new byte[representationLength - 4];
			din.read(representationData, 0, representationData.length);
			try (DataInputStream rdin = new DataInputStream(new ByteArrayInputStream(representationData))) {
				byte[] captureDetails = new byte[14];
				rdin.read(captureDetails, 0, 14);
				byte noOfQualityBlocks = rdin.readByte();
				if (noOfQualityBlocks > 0) {
					byte[] qualityBlocks = new byte[noOfQualityBlocks * 5];
					rdin.read(qualityBlocks, 0, qualityBlocks.length);
				}
				short noOfLandmarkPoints = rdin.readShort();
				byte[] facialInformation = new byte[15];
				rdin.read(facialInformation, 0, 15);
				if (noOfLandmarkPoints > 0) {
					byte[] landmarkPoints = new byte[noOfLandmarkPoints * 8];
					rdin.read(landmarkPoints, 0, landmarkPoints.length);
				}
				byte faceType = rdin.readByte();
				byte imageDataType = rdin.readByte();
				byte[] otherImageInformation = new byte[9];
				rdin.read(otherImageInformation, 0, otherImageInformation.length);
				int lengthOfImageData = rdin.readInt();

				byte[] image = new byte[lengthOfImageData];
				rdin.read(image, 0, lengthOfImageData);

				return image;
			}
		} catch (Exception ex) {
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					ex.getMessage() + ExceptionUtils.getStackTrace(ex));
		}
	}


	private String getCrdentialSubject(String crdential) {
		org.json.JSONObject jsonObject = new org.json.JSONObject(crdential);
		String credentialSubject = jsonObject.get("credentialSubject").toString();
		return credentialSubject;
	}

	private void printStatusUpdate(String requestId, byte[] data, String credentialType, String errorMsg)
			throws DataShareException, ApiNotAccessibleException, IOException, Exception {
//		DataShare dataShare = null;
//		dataShare = dataShareUtil.getDataShare(data, policyId, partnerId);
		CredentialStatusEvent creEvent = new CredentialStatusEvent();
		LocalDateTime currentDtime = DateUtils.getUTCCurrentDateTime();
		StatusEvent sEvent = new StatusEvent();
		sEvent.setId(UUID.randomUUID().toString());
		sEvent.setRequestId(requestId);
		sEvent.setStatus("RECEIVED");
		sEvent.setUrl(null);
		sEvent.setErrorMsg(errorMsg);	
		sEvent.setTimestamp(Timestamp.valueOf(currentDtime).toString());
		creEvent.setPublishedOn(new DateTime().toString());
		creEvent.setPublisher("PRINT_SERVICE_INTERFACE");
		creEvent.setTopic(topic);
		creEvent.setEvent(sEvent);
		
		webSubSubscriptionHelper.printStatusUpdateEvent(topic, creEvent);
	}

	public org.json.JSONObject decryptAttribute(org.json.JSONObject data, String encryptionPin, String credential)
			throws ParseException {

		// org.json.JSONObject jsonObj = new org.json.JSONObject(credential);
		JSONParser parser = new JSONParser(); // this needs the "json-simple" library
		Object obj = parser.parse(credential);
		JSONObject jsonObj = (org.json.simple.JSONObject) obj;

		JSONArray jsonArray = (JSONArray) jsonObj.get("protectedAttributes");
		for (Object str : jsonArray) {

				CryptoWithPinRequestDto cryptoWithPinRequestDto = new CryptoWithPinRequestDto();
				CryptoWithPinResponseDto cryptoWithPinResponseDto = new CryptoWithPinResponseDto();

				cryptoWithPinRequestDto.setUserPin(encryptionPin);
				cryptoWithPinRequestDto.setData(data.getString(str.toString()));
				try {
					cryptoWithPinResponseDto = cryptoUtil.decryptWithPin(cryptoWithPinRequestDto);
				} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					printLogger.error("Error while decrypting the data" ,e);
					throw new CryptoManagerException(PlatformErrorMessages.PRT_INVALID_KEY_EXCEPTION.getCode(),
							PlatformErrorMessages.PRT_INVALID_KEY_EXCEPTION.getMessage(), e);
				}
				data.put((String) str, cryptoWithPinResponseDto.getData());
			
			}

		return data;
	}

	@Override
	public UpdateStatusResponseDto updateCardStatus(CardUpdateRequestDto cardUpdateInput) {
		UpdateStatusResponseDto response = new UpdateStatusResponseDto();
		ErrorDTO error = null;
		printLogger.debug("Data received from perso system : " + cardUpdateInput);
		if (cardUpdateInput.getTopic().equalsIgnoreCase("CREDENTIAL_STATUS_UPDATE")) {
			try {
				printStatusUpdate(cardUpdateInput.getEvent().getTransactionId(), null, cardUpdateInput.getTopic(),
						cardUpdateInput.getEvent().getMsg());
				response.setSuccess(true);
			} catch (Exception e) {
				error = new ErrorDTO();
				error.setErrorCode("500");
				error.setMessage("Error while publishing the data for topic " + cardUpdateInput.getTopic() + " "+ e.getMessage());
				printLogger.error("Error while publishing the data for topic " + cardUpdateInput.getTopic(), e);
				e.printStackTrace();
			}
		}
		if (cardUpdateInput.getTopic().equalsIgnoreCase("CARD_NUMBER_UPDATE")) {
			if (Objects.equals(cardUpdateInput.getEvent().getStatus(), "PRINTED")) {
				try {
					printCardNumberUpdate(cardUpdateInput);
					response.setSuccess(true);
				} catch (Exception e) {
					error = new ErrorDTO();
					error.setErrorCode("500");
					error.setMessage("Error while processing PRINTED status for topic CARD_NUMBER_UPDATE" + ": " + e.getMessage());
					printLogger.error("Error while processing PRINTED status for topic CARD_NUMBER_UPDATE" + ": " + e);
				}
			}

			if (Objects.equals(cardUpdateInput.getEvent().getStatus(), "DELIVERED") || Objects.equals(cardUpdateInput.getEvent().getStatus(), "READY_FOR_DELIVERY")) {
				try {
					NotificationStatus notificationStatus = new NotificationStatus();
					notificationStatus.setNin(cardUpdateInput.getEvent().getNin());
					notificationStatus.setTopic(cardUpdateInput.getEvent().getStatus());
					notificationStatus.setCrDTimes(LocalDateTime.now());

					Map<String, Object> attributes = new HashMap<>();
					attributes.put("district", cardUpdateInput.getEvent().getDistrict());
					attributes.put("county", cardUpdateInput.getEvent().getCounty());

					if (cardUpdateInput.getEvent().getIssuanceDate() != null) {
						SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
						Date date = inputFormat.parse(cardUpdateInput.getEvent().getIssuanceDate());
						SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
						String formattedDate = outputFormat.format(date);
						attributes.put("issuanceDate", formattedDate);
					}
					
					notificationStatus.setAttributes(mapper.writeValueAsString(attributes));
					notificationStatusRepository.save(notificationStatus);

//					sendNotification(cardUpdateInput.getEvent().getNin(), cardUpdateInput.getEvent().getStatus(), attributes, notificationStatus);
					response.setSuccess(true);
				} catch(java.text.ParseException e){
					error = new ErrorDTO();
					error.setErrorCode("500");
					error.setMessage("Invalid Issuance Date format for topic " + cardUpdateInput.getTopic() + ". Expected format: yyyy-MM-dd'T'HH:mm:ssXXX");
					printLogger.error("Invalid Issuance Date format for topic {}", cardUpdateInput.getTopic(), e);
				} catch(Exception e){
					error = new ErrorDTO();
					error.setErrorCode("500");
					error.setMessage("Failed to send notification for transactionId " + cardUpdateInput.getEvent().getTransactionId() + ": " + e.getMessage());
					printLogger.error("Failed to send notification for transactionId {}", cardUpdateInput.getEvent().getTransactionId(),  e);
				}
			}
		}
		if(!response.isSuccess() && error == null ) {
			error = new ErrorDTO();
			error.setErrorCode("500");
			error.setMessage("provided topic  " + cardUpdateInput.getTopic() + " is not supported");			;
		}
		if(error != null) {
			response.setError(error);
			response.setSuccess(false);
		}
		return response;
	}

	private void printCardNumberUpdate(CardUpdateRequestDto cardUpdateInput) {
		CardNumberUpdateDto data = new CardNumberUpdateDto();
		data.setPublishedOn(DateTime.now().toString());
		data.setPublisher("PRINT_SERVICE_INTERFACE");
		data.setTopic("CARD_NUMBER_UPDATE");
		EventDetails eventDetails = new EventDetails();
		eventDetails.setCardNumber(cardUpdateInput.getEvent().getPlasticCardNumber());
		eventDetails.setNin(cardUpdateInput.getEvent().getNin());
		EventTypeDto eventType = new EventTypeDto();
		eventType.setName("CARD_NUMBER_UPDATE");
		eventType.setNamespace("PRINT_SERVICE");
		EventData eventData = new EventData();
		eventData.setData(eventDetails);
		eventData.setDataShareUri(null);
		eventData.setTimestamp(DateTime.now().toString());
		eventData.setTransactionId(cardUpdateInput.getEvent().getTransactionId());
		eventData.setId(cardUpdateInput.getEvent().getRequestId());
		eventData.setType(eventType);
		data.setEvent(eventData);
		webSubSubscriptionHelper.cardNumberPublishEvent(data.getTopic(), data);
	}
	private byte[] convertToJPG(String isoTemplate, boolean isUpscaleRequired) {
		byte[] inputFileBytes = Base64.decodeBase64(isoTemplate);
		int index;
		for (index = 0; index < inputFileBytes.length; index++) {
			if ((char) inputFileBytes[index] == 'j' && (char) inputFileBytes[index + 1] == 'P') {
				break;
			}
		}
		try {
			return convertToJPG(Arrays.copyOfRange(inputFileBytes, index - 4, inputFileBytes.length), "image",
					isUpscaleRequired);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private byte[] convertToJPG(byte[] jp2Data, String fileName, boolean isUpscaleRequired) throws IOException {
		ByteArrayOutputStream beforeUpScale = new ByteArrayOutputStream();
		ByteArrayOutputStream afterUpScale = new ByteArrayOutputStream();
		J2KImageReader j2kImageReader = new J2KImageReader(null);
		j2kImageReader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(jp2Data)));
		ImageReadParam imageReadParam = j2kImageReader.getDefaultReadParam();
		BufferedImage image = j2kImageReader.read(0, imageReadParam);
		ImageIO.write(image, "PNG", beforeUpScale);
		if (!isUpscaleRequired) {
			return beforeUpScale.toByteArray();
		}
		int height = image.getHeight();
		int width = image.getWidth();
		BufferedImage outputImage = createResizedCopy(image, 2 * width, 2 * height, true);
		ImageIO.write(outputImage, "PNG", afterUpScale);
		byte[] jpgImg = afterUpScale.toByteArray();
		return jpgImg;
	}
	private BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight,
			boolean preserveAlpha) {
		System.out.println("resizing...");
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}

	@Override
	public String callPersoService(PersoRequestDto request) {
		// TODO Auto-generated method stub
		return serviceCaller.callPersoService(request);
	}

	private boolean sendNotification(String nin, String topic, Map<String, Object> attributes, NotificationStatus notificationStatus) {
		boolean emailSent = false;
		boolean smsSent = false;
		String remark = null;

		try {
			// set template code
			String emailSubjectTemplateTypeCode = "";
			String emailTemplateTypeCode = "";
			String smsTemplateTypeCode = "";

			if (topic.equals("DELIVERED")) {
				emailSubjectTemplateTypeCode = deliveredEmailSubjectCode;
				emailTemplateTypeCode = deliveredEmailCode;
				smsTemplateTypeCode = deliveredSmsCode;
			} else if (topic.equals("READY_FOR_DELIVERY")) {
				emailSubjectTemplateTypeCode = readyEmailSubjectCode;
				emailTemplateTypeCode = readyEmailCode;
				smsTemplateTypeCode = readySmsCode;
			}

			Map<String, String> fieldData = null;
			List<CardDetail> cardDetails = cardDetailDao.fetchCardDetailByNin(nin);
			if (cardDetails != null && !cardDetails.isEmpty()) {
				CardDetail cardDetail = cardDetails.get(0);

				List<String> fields = new ArrayList<>();
				fields.add("surname");
				fields.add("givenName");
				fields.add("email");
				fields.add("phone");
				fields.add("residenceStatus");
				fields.add("CountryCode");

				// get Process
				String eventData = cardDetail.getEventData();
				JsonNode rootNode = mapper.readTree(eventData);
				String process = rootNode.at("/event/data/registrationType").asText();

				// If no process in eventData, hit-and-try with all possible processes
				if (process == null || process.isBlank()) {
					String[] processesToTry = {"RENEWAL", "UPDATE", "FIRSTID", "LOST", "NEW"};
					for (String p : processesToTry) {
						try {
							fieldData = utilities.getFields(cardDetail.getRegId(), fields, SOURCE, p);
                            break;
						} catch (ObjectDoesnotExistsException ode) {
							// wrong process, try next
						} catch (PacketManagerException pe) {
							remark = Optional.ofNullable(pe.getLocalizedMessage())
									.filter(msg -> !msg.isBlank())
									.orElse(pe.getClass().getSimpleName());
							// real error, stop trying further
							break;
						}
					}
					if (fieldData == null && remark == null) {
						printLogger.error("No registration process found for nin {}", nin);
					}
				} else {
					try {
						fieldData = utilities.getFields(cardDetail.getRegId(), fields, SOURCE, process);
					} catch (ObjectDoesnotExistsException ode) {
						printLogger.error("No registration process found for nin {} and process {}", nin, process);
					} catch (PacketManagerException pe) {
						remark = Optional.ofNullable(pe.getLocalizedMessage())
								.filter(msg -> !msg.isBlank())
								.orElse(pe.getClass().getSimpleName());
					}
				}
			}

			if (fieldData == null) {
				if (remark == null) {
					remark = "Failed to fetch registration fields";
				}
				return false;
			}

			// fetch identity fields from packet manager response
			String surnameJson = fieldData.get("surname");
			JsonNode jsonArray = mapper.readTree(surnameJson);
			String surnameValue = jsonArray.get(0).get("value").asText();

			String givenNameJson = fieldData.get("givenName");
			jsonArray = mapper.readTree(givenNameJson);
			String givenNameValue = jsonArray.get(0).get("value").asText();

			String maskedNin = "*******" + nin.substring(7, 14);

			attributes.put("surname", surnameValue);
			attributes.put("givenName", givenNameValue);
			attributes.put("maskedNin",maskedNin);

			// send notification
			String email = fieldData.get("email");
			String phoneNo = fieldData.get("phone");

			if (Objects.equals(String.valueOf(attributes.get("district")), "KAMPALA (12)")) {
				Object countyValue = attributes.get("county");
				if (countyValue != null) {
					String district = String.valueOf(attributes.get("district"));
					String county = String.valueOf(countyValue);
					String newDistrict = district + " - " + county;
					attributes.put("district", newDistrict);
				}
			}

			printLogger.info("Attributes Map for nin {} : {}", nin, attributes);

			String residenceStatus = fieldData.get("residenceStatus");
			if (email != null && (residenceStatus == null || "Outside Uganda".equals(residenceStatus))) {
				try {
					EmailResponseDTO emailResp = notificationService.sendEmail(emailTemplateTypeCode, emailSubjectTemplateTypeCode, attributes, email);
					if (emailResp.getStatus().equals("success")) emailSent = true;
				} catch (Exception e) {
					remark = Optional.ofNullable(e.getLocalizedMessage())
                            .filter(msg -> !msg.isBlank())
                            .orElse(e.getClass().getSimpleName());
                    printLogger.error("Failed to send Email notification for the topic {}", topic, e);
                }
            } else emailSent = true;

			String countryCode = fieldData.get("CountryCode");
			if (phoneNo != null && (residenceStatus == null || "In Uganda".equals(residenceStatus)) && countryCode != null && "Uganda (256)".equals(countryCode)) {
				try {
					SmsResponseDTO smsResp = notificationService.sendSMS(smsTemplateTypeCode, attributes, phoneNo);
					if (smsResp.getStatus().equals("success")) smsSent = true;
				} catch (Exception e) {
					remark = Optional.ofNullable(e.getLocalizedMessage())
                            .filter(msg -> !msg.isBlank())
                            .orElse(e.getClass().getSimpleName());
					printLogger.error("Failed to send SMS notification for the topic {}", topic, e);
                }
            } else smsSent = true;

		} catch (Exception e) {
			remark = Optional.ofNullable(e.getLocalizedMessage())
                    .filter(msg -> !msg.isBlank())
                    .orElse(e.getClass().getSimpleName());
			throw new RuntimeException(e);
		} finally {
			if (remark != null && remark.contains("Invalid UIN")) {
		        printLogger.warn("Skipping DB update for invalid NIN");
		    } else {
		    	if (emailSent && smsSent) {
					notificationStatus.setNotificationSent(true);
				} else {
					notificationStatus.setRemark(remark);
				}
				notificationStatus.setIsProcessing(false);
	            notificationStatus.setUpdatedTimes(LocalDateTime.now());
	            notificationStatusRepository.save(notificationStatus);
		    }
	    }
		return emailSent && smsSent;
	}

	public String sendCardToPersoService(String regId) {
		List<CardDetail> cardDetails = cardDetailDao.fetchCardDetailByRegId(regId);
		if (cardDetails != null && !cardDetails.isEmpty()) {
			CardDetail cardDetail = cardDetails.get(0);

			if (cardDetail.getIsReadyToPush() && !cardDetail.getIsProcessing() &&
					!cardDetail.getIsPushed() && cardDetail.getRemark() == null) {
                processSingleRequest(cardDetail);
            }

			if (cardDetail.getIsPushed()) {
				printLogger.info("Card details pushed for regId {}", regId);
				return CardStatusMessage.SENT.format(regId);
			}

			if (cardDetail.getIsProcessing()) {
				printLogger.info("Card details processing is ongoing for regId {}", regId);
				return CardStatusMessage.PROCESSING.format(regId);
			}

			if (cardDetail.getIsFailed() && cardDetail.getRemark() != null) {
				printLogger.warn("Card details for regId {} has remark: {}", regId, cardDetail.getRemark());
				return CardStatusMessage.FAILED.format(regId, cardDetail.getRemark());
			}

			printLogger.warn("Card Details for regId {} is invalid for sending to perso, with remark {}", regId, cardDetail.getRemark());
			return CardStatusMessage.INTERNAL_ERROR.format();

		} else {
			printLogger.warn("No card details found for regId {}", regId);
			return CardStatusMessage.NO_DETAILS.format(regId);
		}
	}
}
