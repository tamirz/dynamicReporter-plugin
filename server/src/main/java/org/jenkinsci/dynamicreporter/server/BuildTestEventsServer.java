package org.jenkinsci.dynamicreporter.server;

import org.jenkinsci.dynamicreporter.server.events.TestReportsReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuildTestEventsServer {

	private static Logger log = LoggerFactory.getLogger(BuildTestEventsServer.class);
	private ExecutorService executorService;
	private final int port;
	private ServerSocket serverSocket;

	public BuildTestEventsServer(int port) {
		this.port = port;
	}

	public void start() throws IOException {
		executorService = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(port);
		executorService.submit(new Runnable() {

			public void run() {
				handleConnections();
			}
		});
	}

	public void stop() throws IOException {
		executorService.shutdown();
		serverSocket.close();
	}

	private void handleConnections() {
		try {
			while (true) {
				final Socket socket = serverSocket.accept();
				executorService.submit(new Runnable() {

					public void run() {
						handleTestRun(socket);
					}

				});
			}
		} catch (IOException e) {
			if (!executorService.isShutdown()) {
				log.error("Error occurred while listening for connections", e);
			}
		} finally {
			try {
				serverSocket.close();
			} catch (IOException ignored) {
			}
		}
	}

	private void handleTestRun(Socket socket) {
		try {
			TestReportsReceiver testReportsReceiver = new TestReportsReceiver(socket.getInputStream(), null);
			testReportsReceiver.run();
		} catch (IOException e) {
			log.error("Error occurred while handling test run", e);
		}
	}

}
