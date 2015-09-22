package org.jenkinsci.plugins.dynamicreporter;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Action used to display the report for the build
 */
public class DynamicReporterRunAction implements Action {

	private final static Logger Log = Logger.getLogger(DynamicReporterRunAction.class.getName());
	private static final String ICON_DYNAMIC_REPORT = "/plugin/dynamicReporter/images/ravello-logo-loader.gif";
	private final File reportsDirectory;
	private final AbstractBuild build;

	public DynamicReporterRunAction(AbstractBuild build, File reportsDirectory) {
		this.build = build;
		this.reportsDirectory = reportsDirectory;
	}

	public AbstractBuild getBuild() {
		return build;
	}

	@JavaScriptMethod
	public long getDynamicReporterLastStamp() {
		Log.info(String.format("Trying to retrieve timestamp from report directory: %s", reportsDirectory.getAbsolutePath()));
		File[] reportsFiles = reportsDirectory.listFiles();
		if (reportsFiles != null && reportsFiles.length > 0) {
			Arrays.sort(reportsFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
			long lastModifiedStamp = reportsFiles[0].lastModified();
			Log.fine(String.format("Trying to retrieve timestamp from report directory: %s", reportsDirectory.getAbsolutePath()));
			return lastModifiedStamp;
		}
		return -1;
	}

	public String getBuildNumber() {
		return String.valueOf(build.getNumber());
	}

	public String getUrlName() {
		return "dynamicreporter";
	}

	public String getDisplayName() {
		return "Dynamic Tests Report";
	}

	public String getIconFileName() {
		return ICON_DYNAMIC_REPORT;
	}

}
