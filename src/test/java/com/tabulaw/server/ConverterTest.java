package com.tabulaw.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.annotations.Test;

@Test
public class ConverterTest {
	private static final String URL = "http://localhost:8080/Tabulaw-converter/converter.html";
	private static final String TEST_FILENAME = "81.html";
	private static final String RESULT_FILENAME = "d:/81.doc";

	public void testConverter() throws Exception {
		HttpClient httpclient = new DefaultHttpClient();

		HttpPost httppost = new HttpPost(URL);

		URL url = getClass().getResource(TEST_FILENAME);
		File fin = new File(url.toURI());

		MultipartEntity multipartEntity = new MultipartEntity();
		InputStreamBody inputStreamBody = new InputStreamBody(new FileInputStream(fin), "text/html", TEST_FILENAME);
		multipartEntity.addPart(TEST_FILENAME, inputStreamBody);

		httppost.setEntity(multipartEntity);
		httppost.addHeader("accept-type", "application/msword");

		System.out.println("executing request " + httppost.getRequestLine());
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (resEntity != null) {
			System.out.println("Response content length: " + resEntity.getContentLength());
			System.out.println("Chunked?: " + resEntity.isChunked());
		}
		if (resEntity != null) {
			File fout = new File(RESULT_FILENAME);
			resEntity.writeTo(new FileOutputStream(fout));
		}

		httpclient.getConnectionManager().shutdown();
	}

}
