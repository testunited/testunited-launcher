package org.testunited.launcher;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testunited.api.TestUnitedTestApplication;
import org.testunited.api.TestUnitedTestExecutionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestUnitedTestLauncher implements TestUnitedTestApplication {
	Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) throws IOException {

		new TestUnitedTestLauncher().run(args);
	}

	private void runTests(List<TestBundle> testBundles) {

		var selectors = new ArrayList<PackageSelector>();

		for (var testBundle : testBundles) {
			selectors.add(DiscoverySelectors.selectPackage(testBundle.testPackage));
		}

		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().selectors(selectors).build();

		var launcher = LauncherFactory.create();

		var testPlan = launcher.discover(request);

		Set<TestIdentifier> testRoots = testPlan.getRoots();

		logger.info("-----Tests Discovered-----");
		for (var testRoot : testRoots) {
			logger.info(">" + testRoot.getDisplayName());
			for (var testClass : testPlan.getChildren(testRoot)) {
				logger.info("->" + testClass.getDisplayName());
				for (var test : testPlan.getChildren(testClass)) {
					logger.info("-->" + test.getDisplayName());
				}
			}
		}

		launcher.execute(request, new TestUnitedTestExecutionListener());
	}

	@Override
	public void run(String... args) {
		TestRunnerArgs testRunnerArgs = TestRunnerArgs.parse(args);
		System.setProperty("env", testRunnerArgs.environment);
		this.runTests(testRunnerArgs.testBundles);
	}
}
