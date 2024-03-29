package org.testunited.launcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testunited.api.TestResultSubmissionSummary;
import org.testunited.api.TestUnitedTestApplication;
import org.testunited.api.TestUnitedTestExecutionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestUnitedTestLauncher implements TestUnitedTestApplication {
	Logger logger = LoggerFactory.getLogger(getClass());
	private static final String SESSION_NAME_KEY = "testunited.testsession.name";

	public static void main(String[] args) throws IOException {

		new TestUnitedTestLauncher().run(args);
	}

	private TestResultSubmissionSummary runTests(List<TestBundle> testBundles) {

		var selectors = new ArrayList<PackageSelector>();

		for (var testBundle : testBundles) {
			selectors.add(DiscoverySelectors.selectPackage(testBundle.testPackage));
		}

		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
				.selectors(selectors).build();

		var launcher = LauncherFactory.create();

		if(logger.isDebugEnabled()){
			var testPlan = launcher.discover(request);
			var testRoots = testPlan.getRoots();
			
			StringBuilder sb = new StringBuilder();
			sb.append("\n-----Tests Discovered-----");
			for (var testRoot : testRoots) {
				sb.append("\n>" + testRoot.getDisplayName());
				for (var testClass : testPlan.getChildren(testRoot)) {
					sb.append("\n-->" + testClass.getLegacyReportingName());
					for (var test : testPlan.getChildren(testClass)) {
						sb.append("\n---->" + test.getDisplayName());
					}
				}
			}
			sb.append("\n-------------------------\n");

			logger.debug(sb.toString());
		}

		var listener = new TestUnitedTestExecutionListener();
		launcher.execute(request, listener);
		
		return listener.getSummary();
	}

	private void callback(String callbackUrl, UUID testSessionId) {
		
		if(callbackUrl == null || callbackUrl.isEmpty()) {
			logger.info("Exiting as no callback url was provided.");
			return;
		}
		
		HttpClient httpclient = HttpClients.createDefault();
		StringEntity requestEntity = new StringEntity(testSessionId.toString(), ContentType.APPLICATION_JSON);
		HttpPost postMethod = new HttpPost(callbackUrl);
		postMethod.setEntity(requestEntity);
		HttpResponse rawResponse = null;

		try {
			logger.info("Posting test results to {}.", callbackUrl);
			rawResponse = httpclient.execute(postMethod);

			int http_status_actual = rawResponse.getStatusLine().getStatusCode();

			if (http_status_actual == 200 || http_status_actual == 201) {
				logger.info("SUCCESSFUL: Posting test results to {}.", callbackUrl);
			} else {
				logger.error("FAILED: Posting test results to {}. \n HTTP_STATUS:{}\n{}", callbackUrl,
						rawResponse.getStatusLine().getStatusCode(), rawResponse.getStatusLine().getReasonPhrase());
			}

			HttpEntity entity = rawResponse.getEntity();
			EntityUtils.consume(entity);

		} catch (Exception e) {
			logger.error("FAILED: Posting test results to {}.", callbackUrl);
			e.printStackTrace();
		} finally {
		}
	}
	
	@Override
	public void run(String... args) {
		TestRunnerArgs testRunnerArgs = TestRunnerArgs.parse(args);
		
		if(testRunnerArgs == null) {
			logger.error("Error parsing the arguments.");
			return;
		}
		
		System.setProperty(SESSION_NAME_KEY, testRunnerArgs.sessionName);
		var summary = this.runTests(testRunnerArgs.testBundles);		
		
		this.callback(testRunnerArgs.callbackUrl, summary.getTestSessionId());
	}
}
