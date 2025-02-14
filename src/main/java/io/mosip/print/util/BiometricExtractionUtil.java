package io.mosip.print.util;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.imagedecoder.model.DecoderRequestInfo;
import io.mosip.imagedecoder.model.DecoderResponseInfo;
import io.mosip.imagedecoder.model.Response;
import io.mosip.imagedecoder.openjpeg.OpenJpegDecoder;
import io.mosip.imagedecoder.spi.IImageDecoderApi;



public class BiometricExtractionUtil {



	public static String convertIrisIsoToImage(byte[] isoBytes) throws Exception {
		ConvertRequestDto req = new ConvertRequestDto();
		req.setInputBytes(isoBytes);
		req.setImageType(0);
		req.setPurpose("REGISTRATION");
		req.setVersion("ISO19794_4_2011");
		IrisBDIR irisBDIR = getIrisBDIRISO19794_6_2011(req.getInputBytes(), req.getOnlyImageInformation());
		byte[] isoData = irisBDIR.getRepresentation().getRepresentationData().getImageData().getImage();
		DecoderRequestInfo requestInfo = new DecoderRequestInfo();
		requestInfo.setImageData(isoData);
		requestInfo.setBufferedImage(true);
		IImageDecoderApi decoder = new OpenJpegDecoder();
		Response<DecoderResponseInfo> info = decoder.decode(requestInfo);
		BufferedImage irisImage = info.getResponse().getBufferedImage();

		byte[] imageBytes = ((DataBufferByte) irisImage.getRaster().getDataBuffer()).getData();
		String  data = java.util.Base64.getEncoder().encodeToString(imageBytes);
		return data;

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
