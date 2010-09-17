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

@SuppressWarnings("serial")
public class DocumentConverter extends HttpServlet {
	private static final int TEMPORARY_FOLDER_THRESHOLD_BYTES = 3 * 1024 * 1024;
	private static final String OUT_FORMAT_PARAM_NAME = "accept-type";
	private static final String TMP_DIR_PATH = ".";
	private File tmpDir;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		tmpDir = new File(TMP_DIR_PATH);
		if (!tmpDir.isDirectory()) {
			throw new ServletException(TMP_DIR_PATH + " is not a directory");
		}
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

			// trying to read mime-type from headers first and then from
			// parameters
			String dstMimeType = request.getHeader(OUT_FORMAT_PARAM_NAME);
			if (StringUtils.isEmpty(dstMimeType)) {
				for (FileItem item : items) {
					if (item.getFieldName().equals(OUT_FORMAT_PARAM_NAME)) {
						dstMimeType = item.getString();
					}
				}
			}

			for (FileItem item : items) {
				if (!item.isFormField()) {

					String filename = item.getName();
					if (StringUtils.isEmpty(filename)) {
						continue;
					}

					InputStream is = item.getInputStream();

					String sourceMimeType = item.getContentType();

					if (sourceMimeType == null) {
						throw new ServletException("Unknown content type of uploaded file: " + filename);
					}
					int docFormat = AsposeMimeTypeEnum.getAsposeInputFormatForMimeType(sourceMimeType);

					Document asposeDoc = new Document(is, null, docFormat, null);
					filename = FilenameUtils.getBaseName(filename);

					docFormat = AsposeMimeTypeEnum.getAsposeOutputFormatForMimeType(dstMimeType);

					ByteArrayOutputStream output = new ByteArrayOutputStream();
					asposeDoc.save(output, docFormat);
					response.setContentType(dstMimeType);
					String attachmentHeader = String.format("attachment; filename=%s.%s", filename, AsposeMimeTypeEnum.getOutputExtension(dstMimeType));
					response.setHeader("Content-disposition", attachmentHeader);
					output.writeTo(response.getOutputStream());

					break; // sending back multiple documents doesn't supported
							// yet

				}
			}

			response.setStatus(HttpServletResponse.SC_CREATED);
			response.flushBuffer();
		} catch (ClientAbortException cae) {
			// TODO Add log entry, do not send anything to response
		} catch (Exception e) {
			String emsg = "Unable to digest uploaded files: " + e.getMessage();
			// log.error(emsg, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, emsg);
		}
	}
}