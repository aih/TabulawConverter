package com.tabulaw.server.processor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.aspose.words.LoadFormat;

public class Processor {
	private Map<Integer, AbstractInputStreamProcessor> inputProcessorMap = new HashMap<Integer, AbstractInputStreamProcessor>();

	public Processor() {
		inputProcessorMap.put(LoadFormat.HTML, new HtmlInputStreamProcessor());
	}
	
	public InputStream preProcessInputStream (int format, InputStream inputStream ) throws Exception {
		if (inputProcessorMap.containsKey(format)) {
			AbstractInputStreamProcessor processor = inputProcessorMap.get(format);
			return processor.process(inputStream);
		} else {
			return inputStream;
		}
	}
}
