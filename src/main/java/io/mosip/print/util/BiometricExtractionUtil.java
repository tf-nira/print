package io.mosip.print.util;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.imagedecoder.model.DecoderRequestInfo;
import io.mosip.imagedecoder.model.DecoderResponseInfo;
import io.mosip.imagedecoder.model.Response;
import io.mosip.imagedecoder.openjpeg.OpenJpegDecoder;
import io.mosip.imagedecoder.spi.IImageDecoderApi;



public class BiometricExtractionUtil {

	public static String convertFingerIsoToImage(byte[] isoBytes) throws IOException {
		ConvertRequestDto req = new ConvertRequestDto();
		req.setInputBytes(isoBytes);
		req.setImageType(0);
		req.setPurpose("REGISTRATION");
		req.setVersion("ISO19794_4_2011");
		FingerBDIR fingerBDIR = getFingerBDIRISO19794_4_2011(req.getInputBytes(), req.getOnlyImageInformation());
		byte[] isoData = fingerBDIR.getRepresentation().getRepresentationBody().getImageData().getImage();
		DecoderRequestInfo requestInfo = new DecoderRequestInfo();
		requestInfo.setImageData(isoData);
		requestInfo.setBufferedImage(true);
		IImageDecoderApi decoder = new OpenJpegDecoder();
		Response<DecoderResponseInfo> info = decoder.decode(requestInfo);
		BufferedImage fingerprintImage = info.getResponse().getBufferedImage();

		/*
		 * Step 2: Convert the BufferedImage to FingerprintTemplate Convert
		 * BufferedImage to raw pixel data byte array
		 */
		byte[] imageBytes = ((DataBufferByte) fingerprintImage.getRaster().getDataBuffer()).getData();
		String  data = java.util.Base64.getEncoder().encodeToString(imageBytes);
		return data;

	}
	public static String convertIrisIsoToImage(byte[] isoBytes) throws IOException {
		ConvertRequestDto req = new ConvertRequestDto();
		req.setInputBytes(isoBytes);
		req.setImageType(0);
		req.setPurpose("REGISTRATION");
		req.setVersion("ISO19794_4_2011");
		FingerBDIR fingerBDIR = getFingerBDIRISO19794_4_2011(req.getInputBytes(), req.getOnlyImageInformation());
		byte[] isoData = fingerBDIR.getRepresentation().getRepresentationBody().getImageData().getImage();
		DecoderRequestInfo requestInfo = new DecoderRequestInfo();
		requestInfo.setImageData(isoData);
		requestInfo.setBufferedImage(true);
		IImageDecoderApi decoder = new OpenJpegDecoder();
		Response<DecoderResponseInfo> info = decoder.decode(requestInfo);
		BufferedImage fingerprintImage = info.getResponse().getBufferedImage();

		/*
		 * Step 2: Convert the BufferedImage to FingerprintTemplate Convert
		 * BufferedImage to raw pixel data byte array
		 */
		byte[] imageBytes = ((DataBufferByte) fingerprintImage.getRaster().getDataBuffer()).getData();
		String  data = java.util.Base64.getEncoder().encodeToString(imageBytes);
		return data;

	}

	private static FingerBDIR getFingerBDIRISO19794_4_2011(byte[] isoData, int onlyImageInformation)
			throws IOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(isoData);
				DataInputStream inputStream = new DataInputStream(bais);) {
			FingerBDIR fingerBDIR = null;
			if (onlyImageInformation == 1)
				fingerBDIR = new FingerBDIR(inputStream, true);
			else
				fingerBDIR = new FingerBDIR(inputStream);
			// LOGGER.info("fingerBDIR :: ", fingerBDIR);
			return fingerBDIR;
		}
	}
	
	private static IrisBDIR getIrisBDIRISO19794_6_2011(byte[] isoData, int onlyImageInformation) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(isoData);
				DataInputStream inputStream = new DataInputStream(bais);) {
			IrisBDIR irisBDIR = null;
			if (onlyImageInformation == 1)
				irisBDIR = new IrisBDIR(inputStream, true);
			else
				irisBDIR = new IrisBDIR(inputStream);
			// LOGGER.info("irisBDIR :: ", irisBDIR);
			return irisBDIR;
		}
	}

}
