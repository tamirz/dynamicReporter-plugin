package org.jenkinsci.dynamicreporter.transport;

/**
 * Factory that creates a test messages sender that send messages using TCP.
 * 
 * The port to use is given by the environment variable "DYNAMIC_REPORTER_PORT"
 */
public class SocketMessageSenderFactory implements IMessageSenderFactory {
	private final int port;
	private final String host;
	
	public SocketMessageSenderFactory(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public SocketMessageSenderFactory() {
		String host = System.getProperty("DYNAMIC_REPORTER_HOST");
		if (host == null) {
			host = System.getenv("DYNAMIC_REPORTER_HOST");
		}
		if (host == null) {
			this.host = ""; 
		} else {
			this.host = host;
		}
		String portAsString = System.getProperty("DYNAMIC_REPORTER_PORT");
		if (portAsString == null) {
			portAsString = System.getenv("DYNAMIC_REPORTER_PORT");
		}
		if (portAsString == null || portAsString.length() == 0) {
			this.port = -1;
		} else {
			this.port = Integer.parseInt(portAsString);
		}
	}

	public MessageSender getMessageSender() {
		if (port == -1) {
			return new NullMessageSender();
		} else {
			return new SocketMessageSender(host, port);
		}
	}

}
