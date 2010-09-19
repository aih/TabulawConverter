package com.tabulaw.server.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

public class HtmlInputStreamProcessor extends AbstractInputStreamProcessor {
	private HtmlCleaner cleaner = new HtmlCleaner();

	protected InputStream doProcess(InputStream inputStream) throws Exception{
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		TagNode root = cleaner.clean(reader);
		StringBuilder htmlText = new StringBuilder(); 
		htmlText.append("<html>").append(cleaner.getInnerHtml(root)).append("</html>");
		byte[] htmlBytes = htmlText.toString().getBytes();
		return new ByteArrayInputStream(htmlBytes);
	}

}
