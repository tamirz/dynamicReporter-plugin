package org.jenkinsci.dynamicreporter.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Abstract class used to send test (reports) messages
 */
public abstract class MessageSender {

	private static Logger log = LoggerFactory.getLogger(MessageSender.class);
	private static final int CHUNK_SIZE = 1024;
	protected DataOutputStream dataStreamWriter;

	public void sendReport(String reportPath) throws IOException {
		File reportFile = new File(reportPath);
		doSend(reportFile);
		dataStreamWriter.close();
	}

	protected synchronized void doSend(File reportFile) throws IOException {
		dataStreamWriter.writeUTF(reportFile.getName());
		dataStreamWriter.writeLong(reportFile.length());
		writeFile(reportFile);
	}


	private void writeFile(File file) {
		FileInputStream reader = null;
		try {
			reader = new FileInputStream(file);
			byte[] buffer = new byte[CHUNK_SIZE];
			int pos = 0;
			int bytesRead;
			while ((bytesRead = reader.read(buffer, 0, CHUNK_SIZE)) >= 0) {
				dataStreamWriter.write(buffer, 0, bytesRead);
				dataStreamWriter.flush();
				pos += bytesRead;
			}
		} catch (Throwable t) {
			log.error("Could not write file: {}", file.getName());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public abstract void init() throws IOException;

	public abstract void shutdown() throws IOException;

}
