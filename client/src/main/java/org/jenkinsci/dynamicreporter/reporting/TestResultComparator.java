package org.jenkinsci.dynamicreporter.reporting;

import org.testng.ITestResult;
import java.util.Comparator;

/**
 * Comparator for sorting TestNG test results alphabetically by method name.
 * @author Daniel Dyer
 */
class TestResultComparator implements Comparator<ITestResult> {

	public int compare(ITestResult result1, ITestResult result2) {
		return result1.getName().compareTo(result2.getName());
	}
}
