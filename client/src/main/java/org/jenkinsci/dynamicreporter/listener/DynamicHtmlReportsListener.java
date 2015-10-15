package org.jenkinsci.dynamicreporter.listener;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.dynamicreporter.compression.ZipWriter;
import org.jenkinsci.dynamicreporter.reporting.CustomHTMLReporter;
import org.jenkinsci.dynamicreporter.transport.IMessageSenderFactory;
import org.jenkinsci.dynamicreporter.transport.MessageSender;
import org.jenkinsci.dynamicreporter.transport.SocketMessageSenderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;
import org.uncommons.reportng.ReportNGException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DynamicHtmlReportsListener {

	private static Logger log = LoggerFactory.getLogger(DynamicHtmlReportsListener.class);
	private IMessageSenderFactory messageSenderFactory = new SocketMessageSenderFactory();
	private static final String ZIP_EXTENSION = ".zip";
	private static final String TARGET_REPORT_DIRECTORY = "dynamic-reports";
	private int totalCompleted = 0;

	public void onStart(ITestResult testDetails) {
	}

	public void onSuccess(ITestResult testDetails) {
		handleResult(testDetails);
	}

	public void onFailure(ITestResult testDetails) {
		handleResult(testDetails);
	}

	public void onComplete(ITestResult testDetails) {
		handleResult(testDetails);
	}

	public void onSkip(ITestResult testDetails) {
		handleResult(testDetails);
	}

	private synchronized void handleResult(ITestResult testDetails) {
		int currentCompleted = calculateTotalCompleted(testDetails);
		log.info(String.format("Got amount of completed tests, before: %s and after: %s ", totalCompleted, currentCompleted));
		if (currentCompleted == totalCompleted) {
			return;
		}
		MessageSender sender = messageSenderFactory.getMessageSender();
		try {
			doHandleResult(testDetails, sender);
			sender.shutdown();
			totalCompleted = currentCompleted;
		} catch (Throwable t) {
			log.error("Could not handle test result for: {} , Exception: ", testDetails.getName(), t.getCause());
		}
	}

	private void doHandleResult(ITestResult testResult, MessageSender sender) throws IOException {
		long testId = System.currentTimeMillis();
		String outputDirectory = createOutputDirectory(testResult, String.valueOf(testId));
		String compressedReportPath = "";
		try {
			prepareReport(testResult, outputDirectory, 3);
			compressedReportPath = archiveReport(testResult, String.valueOf(testId), outputDirectory);
			send(compressedReportPath, sender);
		} finally {
			deleteArtifacts(outputDirectory, compressedReportPath);
		}
	}

	private void deleteArtifacts(String outputDirectory, String compressedReportPath) {
		deleteReportDirectory(outputDirectory);
		deleteReport(compressedReportPath);
	}

	private void deleteReport(String compressedReportPath) {
		log.debug("Trying to delete already sent report : {}", compressedReportPath);
		if (!FileUtils.deleteQuietly(new File(compressedReportPath))) {
			log.warn("Report: {} wasn't deleted", compressedReportPath);
		}
	}

	private void deleteReportDirectory(String outputDirectory) {
		log.debug("Trying to delete output report directory: {}", outputDirectory);
		if (!FileUtils.deleteQuietly(new File(outputDirectory))) {
			log.warn("Output report directory: {} wasn't deleted", outputDirectory);
		}
	}

	private String archiveReport(ITestResult testResult, String reportName, String sourceDirectory) throws IOException {
		StringBuilder targetReportDirectory = new StringBuilder(testResult.getTestContext().getOutputDirectory());
		targetReportDirectory.append(File.separator).append(TARGET_REPORT_DIRECTORY).append(File.separator);
		FileUtils.forceMkdir(new File(targetReportDirectory.toString()));
		String targetReportFilename = targetReportDirectory.toString() + reportName + ZIP_EXTENSION;
		ZipWriter zipWriter = new ZipWriter(targetReportFilename, sourceDirectory);
		zipWriter.zipDirectory();
		return targetReportFilename;
	}

	private void prepareReport(ITestResult testResult, String outputDirectory, int retriesCount) {
		if (retriesCount < 1) {
			return;
		}
		CustomHTMLReporter reporter = new CustomHTMLReporter();
		ITestContext testContext = testResult.getTestContext();
		List<XmlSuite> xmlSuites = Arrays.asList(testContext.getCurrentXmlTest().getSuite());
		List<ISuite> suites = Arrays.asList(testContext.getSuite());
		try {
			reporter.generateReport(xmlSuites, suites, outputDirectory);
		} catch (ReportNGException reportNGException) {
			log.trace("failed to create report trying again shortly, current retry: {}", retriesCount);
			prepareReport(testResult, outputDirectory, --retriesCount);
		}

	}

	private String createOutputDirectory(ITestResult testResult, String postfix) throws IOException {
		StringBuilder outputDirectory = new StringBuilder(testResult.getTestContext().getOutputDirectory());
		outputDirectory.append(File.separator).append(postfix);
		FileUtils.forceMkdir(new File(outputDirectory.toString()));
		return outputDirectory.toString();
	}


	private void send(String compressedReportPath, MessageSender sender) throws IOException {
		sender.init();
		sender.sendReport(compressedReportPath);
	}

	public boolean shouldSkip(ITestResult testDetails) {
		return !Boolean.valueOf(System.getProperty("use.dynamic.reporter"));
	}

	private int calculateTotalCompleted(ITestResult testResult) {
		ISuite suite = testResult.getTestContext().getSuite();
		int completedTests = 0;
		for (ISuiteResult result : suite.getResults().values()) {
			completedTests += result.getTestContext().getSkippedTests().size() +
					result.getTestContext().getFailedTests().size() + result.getTestContext().getPassedTests().size();
		}
		return completedTests;
	}

}
