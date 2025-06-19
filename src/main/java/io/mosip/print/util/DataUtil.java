package io.mosip.print.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.opencsv.CSVReader;

import io.mosip.print.logger.PrintLogger;

public class DataUtil {

	Logger printLogger = PrintLogger.getLogger(DataUtil.class);
	private static final Map<String, String> parishAndVillage = new HashMap<>();

	static {
		loadInitialRecords(); // This runs only once when class is loaded
	}

	private static void loadInitialRecords() {
		Resource resource = new ClassPathResource("parishandvillage.csv");
		Reader reader;
		try {
			reader = new InputStreamReader(resource.getInputStream());

			try (CSVReader csvReader = new CSVReader(reader)) {
			String[] line;
			while ((line = csvReader.readNext()) != null) {
				if (line.length >= 2) {
					parishAndVillage.put(line[1], line[0]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} catch (IOException e) {

		e.printStackTrace();
	}
	}

	public static String getParishOrVillageCorrectData(String inputData) {
		if (inputData != null) {
			String outputData = parishAndVillage.get(inputData);
			if (outputData != null) {
				return outputData;
			}
		}
		return inputData;

	}

}
