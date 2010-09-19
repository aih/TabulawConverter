package com.tabulaw.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.aspose.words.LoadFormat;
import com.aspose.words.SaveFormat;

public enum AsposeMimeTypeEnum {
	DOC(LoadFormat.DOC, SaveFormat.DOC),
	RTF(LoadFormat.RTF, SaveFormat.RTF),
	DOCX(LoadFormat.DOCX, SaveFormat.DOCX),
	ODT(LoadFormat.ODT, SaveFormat.ODT),
	HTML(LoadFormat.HTML, SaveFormat.HTML);

	private static final String INPUT_CONFIG = "input.mimetypes.properties";
	private static final String OUTPUT_CONFIG = "output.mimetypes.properties";

	private static Map<String, Integer> outputFormats;
	private static Map<String, Integer> inputFormats = new HashMap<String, Integer>();

	private static Properties inputProps = new Properties();
	private static Properties outputProps = new Properties();

	private int inputFormatId;
	private int outputFormatId;

	static {
		outputFormats = new HashMap<String, Integer>();
		inputFormats = new HashMap<String, Integer>();

		inputProps = new Properties();
		outputProps = new Properties();

		try {
			inputProps.load(getResourceAsStream(INPUT_CONFIG));
			outputProps.load(getResourceAsStream(OUTPUT_CONFIG));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// populate translation maps
		for (AsposeMimeTypeEnum v : AsposeMimeTypeEnum.values()) {
			String currentPropertyName = v.getPropertyName();
			if (inputProps != null) {
				String inputMimeTypes = inputProps.getProperty(currentPropertyName);
				convertProperty(inputMimeTypes, inputFormats, v.inputFormatId);
			}

			if (outputProps != null) {
				String outputMimeTypes = outputProps.getProperty(currentPropertyName);
				convertProperty(outputMimeTypes, outputFormats, v.outputFormatId);
			}
		}

	}

	private AsposeMimeTypeEnum(int inputFormatId, int outputFormatId) {
		this.inputFormatId = inputFormatId;
		this.outputFormatId = outputFormatId;
	}

	private static void convertProperty(String mimeTypes, Map<String, Integer> formats, int formatId) {
		if (!StringUtils.isEmpty(mimeTypes)) {
			String[] formatArray = mimeTypes.split(";");
			for (String format : formatArray) {
				if (!formats.containsKey(format)) {
					formats.put(format, formatId);
				}
			}
		}

	}

	public static int getAsposeInputFormatForMimeType(String mimeType) {
		return inputFormats.containsKey(mimeType)?inputFormats.get(mimeType):LoadFormat.UNKNOWN;
	}

	public static int getAsposeOutputFormatForMimeType(String mimeType) {
		return outputFormats.containsKey(mimeType)?outputFormats.get(mimeType):SaveFormat.NONE;
	}

	public static String getOutputExtension(String mimeType) {
		String extension = null;
		int outputFormatId = getAsposeOutputFormatForMimeType(mimeType);
		for (AsposeMimeTypeEnum v : AsposeMimeTypeEnum.values()) {
			if (v.outputFormatId==outputFormatId) {
				extension = v.name().toLowerCase();
			}
		}
		return extension;
	}

	private static InputStream getResourceAsStream(String filename) throws Exception {
		return ResourceLoader.getResourceAsStream(filename);
	}

	public String getPropertyName() {
		return name().toLowerCase().replaceAll("_", ".");
	}

}