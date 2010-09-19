package com.tabulaw.server.processor;

import java.io.InputStream;

public abstract class AbstractInputStreamProcessor {
	public InputStream process(InputStream inputStream) throws Exception {
		return doProcess(inputStream);
	}

	protected abstract InputStream doProcess(InputStream inputStream) throws Exception;

}
