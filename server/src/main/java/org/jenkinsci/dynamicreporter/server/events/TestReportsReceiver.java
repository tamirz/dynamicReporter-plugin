package org.jenkinsci.dynamicreporter.server.events;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jenkinsci.dynamicreporter.server.messages.ITestMessagesHandler;
import org.jenkinsci.dynamicreporter.server.messages.TestReportsMessagesHandler;

/**
 * Receives test messages from an InputStream.
 */
public class TestReportsReceiver implements Runnable {

	private final InputStream in;
	private final String reportsDirectory;

	public TestReportsReceiver(InputStream in, String reportsDirectory) {
		this.in = in;
		this.reportsDirectory = reportsDirectory;
	}

	public void run() {
		ITestMessagesHandler parser = new TestReportsMessagesHandler(reportsDirectory);
		try {
			parser.processTestMessages(new DataInputStream(in));
		} catch (IOException ignored) {
		}
	}
}
