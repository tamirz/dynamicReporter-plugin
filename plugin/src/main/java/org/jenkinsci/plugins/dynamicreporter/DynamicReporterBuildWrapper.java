package org.jenkinsci.plugins.dynamicreporter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.remoting.forward.Forwarder;
import hudson.remoting.forward.ListeningPort;
import hudson.remoting.forward.PortForwarder;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.dynamicreporter.server.events.TestReportsReceiver;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This build wrapper communicates to the process being started the port to use
 * to report test progress (using a env var). It also forward test messages from
 * slave to the master and process them.
 */
public class DynamicReporterBuildWrapper extends BuildWrapper {

	private final static Logger Log = Logger.getLogger(DynamicReporterBuildWrapper.class.getName());
	private static final String REPORTS_PREFIX = "dynamic-reports";
	private static final String JENKINS_HOME = "JENKINS_HOME";

	@DataBoundConstructor
	public DynamicReporterBuildWrapper() {
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
			InterruptedException {
		final File reportsDirectory = new File(build.getRootDir(), REPORTS_PREFIX);
		createReportsDirectory(reportsDirectory, build.getNumber());
		final ListeningPort listeningPort = createPortForwarder(launcher.getChannel(), 0, new ForwarderImpl(
				reportsDirectory.getAbsolutePath()));
		Log.info(String.format("Using DynamicReporter port: %s", listeningPort.getPort()));
		DynamicReporterRunAction dynamicReporterRunAction = new DynamicReporterRunAction(build, reportsDirectory);
		build.addAction(dynamicReporterRunAction);

		return new Environment() {

			@Override
			public void buildEnvVars(Map<String, String> env) {
				env.put("DYNAMIC_REPORTER_PORT", String.valueOf(listeningPort.getPort()));
			}

			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				listeningPort.close();
				return true;
			}

		};
	}

	private void createReportsDirectory(File reportsDirectory, int number) throws IOException {
		FileUtils.forceMkdir(reportsDirectory);
		StringBuilder targetDirectory = new StringBuilder(reportsDirectory.getAbsolutePath()).append(File.separator).append("html");
		final String JENKINS_HOME = System.getenv().get(DynamicReporterBuildWrapper.JENKINS_HOME);
		StringBuilder link = new StringBuilder().append(JENKINS_HOME).append(File.separator).append("userContent").append(
				File.separator).append(number);
		Files.createSymbolicLink(new File(link.toString()).toPath(), new File(targetDirectory.toString()).toPath());
	}

	/**
	 * Same than PortForwarder.create but also works when build is running on master
	 */
	public static ListeningPort createPortForwarder(VirtualChannel ch, final int acceptingPort, Forwarder forwarder)
			throws IOException, InterruptedException {
		// need a remote reference
		final Forwarder proxy = ch.export(Forwarder.class, forwarder);

		return ch.call(new Callable<ListeningPort, IOException>() {
			public ListeningPort call() throws IOException {
				PortForwarder portForwarder = new PortForwarder(acceptingPort, proxy);
				portForwarder.start();
				if (Channel.current() != null) {
					// running on slave
					return Channel.current().export(ListeningPort.class, portForwarder);
				}
				// running on master
				return portForwarder;
			}
		});
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		public DescriptorImpl() {
			super(DynamicReporterBuildWrapper.class);
			load();
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Show Dynamic Reports";
		}

	}

	/**
	 * Forward from slave to master
	 */
	private static class ForwarderImpl implements Forwarder {

		private String reportsDirectory;

		public ForwarderImpl(String reportsDirectory) {
			this.reportsDirectory = reportsDirectory;
		}

		public OutputStream connect(OutputStream out) throws IOException {
			Pipe pipe = Pipe.open();
			Pipe.SinkChannel sinkChannel = pipe.sink();
			Pipe.SourceChannel sourceChannel = pipe.source();
			OutputStream pipedOutputStream = Channels.newOutputStream(sinkChannel);
			InputStream pipedInputStream = Channels.newInputStream(sourceChannel);
			
			Runnable runnable = new TestReportsReceiver(pipedInputStream, reportsDirectory);
			Thread thread = new Thread(runnable);
			thread.setName("Test Reports Receiver");
			thread.start();
			return new RemoteOutputStream(pipedOutputStream);
		}

		/**
		 * When sent to the remote node, send a proxy.
		 */
		private Object writeReplace() {
			return Channel.current().export(Forwarder.class, this);
		}
	}

}
