package org.jenkinsci.dynamicreporter.server.messages;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class TestReportsMessagesHandler implements ITestMessagesHandler {

	private final static Logger Log = Logger.getLogger(TestReportsMessagesHandler.class.getName());
	private final String reportsDirectory;

	public TestReportsMessagesHandler(String reportsDirectory) {
		this.reportsDirectory = reportsDirectory;
	}

	public void processTestMessages(DataInput dataInput) throws IOException {
		File file = saveReportFile(dataInput);
		extractReports(file);
		deleteFile(file);
	}

	private void deleteFile(File file) {
		FileUtils.deleteQuietly(file);
	}

	private void extractReports(File file) {
		try {
			ZipFile zipFile = new ZipFile(file.getAbsoluteFile());
			zipFile.extractAll(reportsDirectory);
		} catch (ZipException ignored) {
			Log.warning("Cannot extract reports for file: " + file.getName());
		}
	}

	private File saveReportFile(DataInput dataInput) throws IOException {
		String name = dataInput.readUTF();
		File file = new File(reportsDirectory, name);
		long fileSize = dataInput.readLong();
		byte[] bytes = new byte[(int) fileSize];
		FileOutputStream fos = new FileOutputStream(file);
		int count;
		while ((count = ((DataInputStream)dataInput).read(bytes)) > 0) {
			fos.write(bytes, 0, count);
		}
		fos.flush();
		fos.close();
		return file;
	}


}
