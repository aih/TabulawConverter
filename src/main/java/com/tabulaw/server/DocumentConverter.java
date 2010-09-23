package com.tabulaw.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.aspose.words.Document;
import com.tabulaw.config.AsposeMimeTypeEnum;
import com.tabulaw.server.processor.Processor;

@SuppressWarnings("serial")
public class DocumentConverter extends HttpServlet {
	private static final int TEMPORARY_FOLDER_THRESHOLD_BYTES = 3 * 1024 * 1024;
	private static final String OUT_FORMAT_PARAM_NAME = "accept-type";
	private static final String TMP_DIR_PATH = ".";
	private File tmpDir;
	private Processor processor;

	private FileItem getFileItem(List<FileItem> items) throws ServletException {
		for (FileItem item : items) {
			if (!item.isFormField()) {

				String filename = item.getName();
				if (StringUtils.isEmpty(filename)) {
					continue;
				}

				String sourceMimeType = item.getContentType();

				if (sourceMimeType == null) {
					throw new ServletException("Unknown content type of uploaded file: " + filename);
				}

				return item; // sending back multiple documents doesn't
				// supported yet

			}
		}
		return null;
	}

	private void write(String filename, String dstMimeType, ByteArrayOutputStream output, HttpServletResponse response)
			throws Exception {

		filename = FilenameUtils.getBaseName(filename);

		response.setContentType(dstMimeType);
		String attachmentHeader = String.format("attachment; filename =\"%s.%s\"", filename, AsposeMimeTypeEnum
				.getOutputExtension(dstMimeType));
		response.setHeader("Content-disposition", attachmentHeader);

		output.writeTo(response.getOutputStream());

		response.setStatus(HttpServletResponse.SC_CREATED);
		response.flushBuffer();

	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		tmpDir = new File(TMP_DIR_PATH);
		if (!tmpDir.isDirectory()) {
			throw new ServletException(TMP_DIR_PATH + " is not a directory");
		}
		processor = new Processor();  
	}

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			java.io.IOException {
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		factory.setSizeThreshold(TEMPORARY_FOLDER_THRESHOLD_BYTES);
		/*
		 * Set the temporary directory to store the uploaded files of size above
		 * threshold.
		 */
		factory.setRepository(tmpDir);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// Parse the request
		try {
			List<FileItem> items = upload.parseRequest(request);

			// trying to read mime type from headers first and then from parameters
			String dstMimeType = request.getHeader(OUT_FORMAT_PARAM_NAME);
			if (StringUtils.isEmpty(dstMimeType)) {
				for (FileItem item : items) {
					if (item.getFieldName().equals(OUT_FORMAT_PARAM_NAME)) {
						dstMimeType = item.getString();
					}
				}
			}
			FileItem item = getFileItem(items);
			String filename = item.getName();
			String sourceMimeType = item.getContentType();

			int srcDocFormat = AsposeMimeTypeEnum.getAsposeInputFormatForMimeType(sourceMimeType);

			InputStream inputStream = processor.preProcessInputStream(srcDocFormat, item.getInputStream());

			Document asposeDoc = new Document(inputStream, null, srcDocFormat, null);

			int dstDocFormat = AsposeMimeTypeEnum.getAsposeOutputFormatForMimeType(dstMimeType);

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			asposeDoc.save(output, dstDocFormat);

			write(filename, dstMimeType, output, response);

		} catch (ClientAbortException cae) {
			// TODO Add log entry, do not send anything to response
		} catch (Exception e) {
			String emsg = "Unable to digest uploaded files: " + e.getMessage();
			// log.error(emsg, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, emsg);
		}
	}

}