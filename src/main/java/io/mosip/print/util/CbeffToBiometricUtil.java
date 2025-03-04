package io.mosip.print.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import io.mosip.print.constant.BiometricType;
import io.mosip.print.constant.LoggerFileConstant;
import io.mosip.print.entity.BIR;
import io.mosip.print.exception.BiometricException;
import io.mosip.print.exception.BiometricTagMatchException;
import io.mosip.print.exception.PlatformErrorMessages;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.model.KeyValuePair;
import io.mosip.print.service.impl.BioApiImpl;
import io.mosip.print.service.impl.CbeffImpl;
import io.mosip.print.spi.CbeffUtil;
import io.mosip.print.spi.IBioApi;

/**
 * The Class CbeffToBiometricUtil.
 * 
 * @author M1048358 Alok
 * @author M1030448 Jyoti
 */
public class CbeffToBiometricUtil {

	/** The print logger. */
	Logger printLogger = PrintLogger.getLogger(CbeffToBiometricUtil.class);

	/** The cbeffutil. */
	private CbeffUtil cbeffutil = new CbeffImpl();
	/** the bioApi */
	private IBioApi bioAPi = new BioApiImpl();

	/**
	 * Instantiates a new cbeff to biometric util.
	 *
	 * @param cbeffutil the cbeffutil
	 */
	public CbeffToBiometricUtil(CbeffUtil cbeffutil) {
		this.cbeffutil = cbeffutil;
	}

	/**
	 * Instantiates biometric util
	 * 
	 */
	public CbeffToBiometricUtil() {

	}

	/**
	 * Gets the photo.
	 *
	 * @param cbeffFileString the cbeff file string
	 * @param type            the type
	 * @param subType         the sub type
	 * @return the photo
	 * @throws Exception the exception
	 */
	public byte[] getImageBytes(String cbeffFileString, String type, List<String> subType) throws Exception {
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"CbeffToBiometricUtil::getImageBytes()::entry");

		byte[] photoBytes = null;
		if (cbeffFileString != null) {
			List<BIR> bIRTypeList = getBIRTypeList(cbeffFileString);
			photoBytes = getPhotoByTypeAndSubType(bIRTypeList, type, subType);
		}
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"CbeffToBiometricUtil::getImageBytes()::exit");

		return photoBytes;
	}

	/**
	 * Gets the photo by type and sub type.
	 *
	 * @param bIRTypeList the b IR type list
	 * @param type        the type
	 * @param subType     the sub type
	 * @return the photo by type and sub type
	 */
	private byte[] getPhotoByTypeAndSubType(List<BIR> bIRList, String type, List<String> subType) {
		byte[] photoBytes = null;
		for (BIR bir : bIRList) {
			if (bir.getBdbInfo() != null) {
				List<BiometricType> singleTypeList = bir.getBdbInfo().getType();
				List<String> subTypeList = bir.getBdbInfo().getSubtype();

				boolean isType = isBiometricType(type, singleTypeList);
				boolean isSubType = isSubType(subType, subTypeList);

				if (isType && isSubType) {
					printLogger.info("type is present"+type);
					photoBytes = bir.getBdb();
					break;
				}
			}
		}
		return photoBytes;
	}

	/**
	 * Checks if is sub type.
	 *
	 * @param subType     the sub type
	 * @param subTypeList the sub type list
	 * @return true, if is sub type
	 */
	private boolean isSubType(List<String> subType, List<String> subTypeList) {
		return subTypeList.equals(subType) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Checks if is single type.
	 *
	 * @param type           the type
	 * @param singleTypeList the single type list
	 * @return true, if is single type
	 */
	private boolean isSingleType(String type, List<SingleType> singleTypeList) {
		boolean isType = false;
		for (SingleType singletype : singleTypeList) {
			if (singletype.value().equalsIgnoreCase(type)) {
				isType = true;
			}
		}
		return isType;
	}

	private boolean isBiometricType(String type, List<BiometricType> biometricTypeList) {
		boolean isType = false;
		for (BiometricType biometricType : biometricTypeList) {
			if (biometricType.value().equalsIgnoreCase(type)) {
				isType = true;
			}
		}
		return isType;
	}

	/**
	 * Merge cbeff.
	 *
	 * @param cbeffFile1 the cbeff file 1
	 * @param cbeffFile2 the cbeff file 2
	 * @return the input stream
	 * @throws Exception the exception
	 */

	public InputStream mergeCbeff(String cbeffFile1, String cbeffFile2) throws Exception {
		byte[] mergedCbeffByte = null;
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"CbeffToBiometricUtil::mergeCbeff()::entry");

		List<BIR> file1BirList = getBIRTypeList(cbeffFile1);
		List<BIR> file2BirList = getBIRTypeList(cbeffFile2);

		if (isBiometricTypeSame(file1BirList, file2BirList)) {
			throw new BiometricTagMatchException(PlatformErrorMessages.PRT_UTL_BIOMETRIC_TAG_MATCH.getCode());
		}

		file1BirList.addAll(file2BirList);
		mergedCbeffByte = cbeffutil.createXML(file2BirList);

		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"CbeffToBiometricUtil::mergeCbeff()::exit");

		return new ByteArrayInputStream(mergedCbeffByte);
	}

	/**
	 * Check file versions.
	 *
	 * @param cbeffFile1 the cbeff file 1
	 * @param cbeffFile2 the cbeff file 2
	 * @return true, if successful
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException                 the SAX exception
	 * @throws IOException                  Signals that an I/O exception has
	 *                                      occurred.
	 */
	/*
	 * private boolean checkFileVersions(String cbeffFile1, String cbeffFile2)
	 * throws ParserConfigurationException, SAXException, IOException { InputSource
	 * source1 = new InputSource(); source1.setCharacterStream(new
	 * StringReader(cbeffFile1));
	 * 
	 * InputSource source2 = new InputSource(); source2.setCharacterStream(new
	 * StringReader(cbeffFile2));
	 * 
	 * DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	 * dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	 * DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); Document doc1 =
	 * dBuilder.parse(source1); Document doc2 = dBuilder.parse(source2); NodeList
	 * cbeffVersion1 = doc1.getElementsByTagName("CBEFFVersion"); NodeList
	 * cbeffVersion2 = doc2.getElementsByTagName("CBEFFVersion");
	 * 
	 * String version1 =
	 * cbeffVersion1.item(0).getChildNodes().item(0).getNextSibling().getTextContent
	 * (); String version2 =
	 * cbeffVersion2.item(0).getChildNodes().item(0).getNextSibling().getTextContent
	 * ();
	 * 
	 * return version1.equals(version2) ? Boolean.TRUE : Boolean.FALSE; }
	 */

	/**
	 * Checks if is same type.
	 *
	 * @param file1BirTypeList the file 1 bir type list
	 * @param file2BirTypeList the file 2 bir type list
	 * @return true, if is same type
	 */
	private boolean isBiometricTypeSame(List<BIR> file1BirList, List<BIR> file2BirList) {
		boolean isTypeSame = false;
		for (BIR bir1 : file1BirList) {
			List<BiometricType> biometricTypeList1 = bir1.getBdbInfo().getType();
			for (BIR bir2 : file2BirList) {
				List<BiometricType> biometricTypeList2 = bir2.getBdbInfo().getType();
				if (biometricTypeList1.equals(biometricTypeList2)) {
					isTypeSame = true;
					break;
				}
			}
			if (isTypeSame)
				break;
		}
		return isTypeSame;
	}

	/**
	 * Extract cbeff with types.
	 *
	 * @param cbeffFile the cbeff file
	 * @param types     the types
	 * @return the input stream
	 * @throws Exception the exception
	 */
	public InputStream extractCbeffWithTypes(String cbeffFile, List<String> types) throws Exception {
		List<BIR> extractedBIR = new ArrayList<>();
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"CbeffToBiometricUtil::extractCbeffWithTypes()::entry");

		byte[] newCbeffByte = null;
		List<BIR> file2BirTypeList = getBIRTypeList(cbeffFile);
		for (BIR bir : file2BirTypeList) {
			List<BiometricType> biometricTypeList = bir.getBdbInfo().getType();
			for (String type : types) {
				if (biometricTypeList.get(0).value().equalsIgnoreCase(type)) {
					extractedBIR.add(bir);
				}
			}
		}
		if (!extractedBIR.isEmpty()) {
			newCbeffByte = cbeffutil.createXML(extractedBIR);
		} else {
			return null;
		}
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"CbeffToBiometricUtil::extractCbeffWithTypes()::exit");

		return new ByteArrayInputStream(newCbeffByte);
	}

	/**
	 * Convert BIRTYP eto BIR.
	 *
	 * @param listOfBIR the list of BIR
	 * @return the list
	 *//*
		 * public List<BIR> convertBIRTYPEtoBIR(List<BIRType> listOfBIR) {
		 * 
		 * return cbeffutil.convertBIRTypeToBIR(listOfBIR); }
		 */

	/**
	 * Gets the BIR type list.
	 *
	 * @param cbeffFileString the cbeff file string
	 * @return the BIR type list
	 * @throws Exception the exception
	 */

	public List<BIR> getBIRTypeList(String cbeffFileString) throws Exception {
		return cbeffutil.getBIRDataFromXML(Base64.decodeBase64(cbeffFileString));
	}

	/**
	 * Gets the BIR type list.
	 * 
	 * @param xmlBytes byte array of XML data
	 * @return the BIR type list
	 * @throws Exception the exception
	 */
	public List<BIR> getBIRDataFromXML(byte[] xmlBytes) throws Exception {
		return cbeffutil.getBIRDataFromXML(xmlBytes);
	}

	/**
	 * Gets the BIR template
	 * 
	 * @param sample the sample
	 * @param flags  the flags
	 * @return the biometric record
	 * @throws BiometricException
	 */
	public BIR extractTemplate(BIR sample, KeyValuePair[] flags) throws BiometricException {
		return bioAPi.extractTemplate(sample, flags).getResponse();
	}

}
