package org.jenkinsci.dynamicreporter.transport;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Send test messages using socket
 */
public class SocketMessageSender extends MessageSender {
	private final String host;
	private final int port;
	private Socket socket;

	public SocketMessageSender(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void init() throws IOException {
		connect(500);
		dataStreamWriter = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

	private void connect(long timeout) throws IOException {
		socket = null;
		long time1 = System.currentTimeMillis();
		do {
			try {
				socket = new Socket(host, port);
			} catch (ConnectException e) {
				// wait for the server to listen a little bit 
				long time2 = System.currentTimeMillis(); 
				if (time2-time1 > timeout) {
					throw e;
				} else {
					try {
						Thread.sleep(50);
					} catch (InterruptedException ie) {
					}
				}
			}
		} while (socket == null);
	}
	
	public void shutdown() throws IOException {
		if (dataStreamWriter != null) {
			dataStreamWriter.close();
			dataStreamWriter = null;
		}
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}
}
