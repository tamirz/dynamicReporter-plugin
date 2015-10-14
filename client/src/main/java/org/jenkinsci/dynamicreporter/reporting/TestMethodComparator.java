package org.jenkinsci.dynamicreporter.reporting;

import org.testng.ITestNGMethod;
import java.util.Comparator;

/**
 * Comparator for sorting TestNG test methods.  Sorts method alphabetically
 * (first by fully-qualified class name, then by method name).
 * @author Daniel Dyer
 */
class TestMethodComparator implements Comparator<ITestNGMethod> {

	public int compare(ITestNGMethod method1, ITestNGMethod method2) {
		int compare = method1.getRealClass().getName().compareTo(method2.getRealClass().getName());
		if (compare == 0) {
			compare = method1.getMethodName().compareTo(method2.getMethodName());
		}
		return compare;
	}
}