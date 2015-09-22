package org.jenkinsci.dynamicreporter.transport;


import java.io.IOException;

/**
 * {@link MessageSender} that do not send messages
 */
public class NullMessageSender extends MessageSender {

	public NullMessageSender() {
		this.dataStreamWriter = new NullWriter(null);
	}

	@Override
	public void init() throws IOException {
	}

	@Override
	public void shutdown() throws IOException {
	}
}
