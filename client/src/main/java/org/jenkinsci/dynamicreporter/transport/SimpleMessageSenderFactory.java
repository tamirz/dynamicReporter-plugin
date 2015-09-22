package org.jenkinsci.dynamicreporter.transport;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Creates {@link MessageSender} that send test message to a {@link java.io.PrintWriter}
 */
public class SimpleMessageSenderFactory implements IMessageSenderFactory {
	private DataOutputStream dataOutputStream;

	public SimpleMessageSenderFactory(DataOutputStream dataOutputStream) {
		this.dataOutputStream = dataOutputStream;
	}

	public MessageSender getMessageSender() {
		return new SimpleMessageSender(dataOutputStream);
	}

	private static class SimpleMessageSender extends MessageSender {

		public SimpleMessageSender(DataOutputStream pw) {
			this.dataStreamWriter = pw;
		}

		@Override
		public void init() throws IOException {
		}

		@Override
		public void shutdown() throws IOException {
		}
	}

}
