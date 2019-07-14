package org.testunited.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRunnerArgs {
	private static Logger logger = LoggerFactory.getLogger(TestRunnerArgs.class);

	List<TestBundle> testBundles;
	TestBundleResolutionMode resolutionMode;
	String callbackUrl;
	String sessionId;
	
	private static String ARG_TEST_BUNDLE_IDS = "TEST_BUNDLE_IDS";
	private static String ARG_TEST_BUNDLE_MODE = "TEST_BUNDLE_MODE";
	private static String ARG_CALLBACK_URL = "CALLBACK_URL";
	private static String ARG_SESSION_ID = "SESSION_ID";

	private static final TestBundleResolutionMode DEFAULT_RESOLUTION_MODE = TestBundleResolutionMode.Classpath;
	private static final String DEFAULT_ENV = "";

	public static HashMap<String, String> getArgValues(String... args) throws IllegalArgumentException {
		HashMap<String, String> argValues = new HashMap<String, String>();

		for (String s : args) {
			String[] argVal = s.split("=");

			try {
				argValues.put(argVal[0], argVal[1]);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		return argValues;
	}

	public static ArrayList<TestBundle> getTestBundles(String testBundlesArg) {
		ArrayList<TestBundle> testBundles = new ArrayList<>();

		if (testBundlesArg == null | testBundlesArg.isEmpty()) {
			logger.info("no test bundles given");
			return null;
		}

		String[] testBundleArray = testBundlesArg.split(";");

		for (String s : testBundleArray) {
			String[] testBundleElements = s.split(":");
			try {
				TestBundle testBundle = new TestBundle(testBundleElements[0], testBundleElements[1],
						testBundleElements[2], testBundleElements[3]);
				testBundles.add(testBundle);
			} catch (Exception ex) {
				logger.error("Error parsing the test bundles {}", s, ex);
			}
		}
		return testBundles;
	}

	private static TestBundleResolutionMode getResolutionMode(String resolutionArg) {
		TestBundleResolutionMode resolutionMode;

		switch (resolutionArg.toLowerCase()) {
		case "local":
			resolutionMode = TestBundleResolutionMode.Local;
			break;
		case "remote":
			resolutionMode = TestBundleResolutionMode.Remote;
			break;
		default:
			resolutionMode = DEFAULT_RESOLUTION_MODE;
		}

		return resolutionMode;
	}

	private static String getCallbackUrl(String envArg) {
		return envArg;
	}

	private static String getSessionId(String sessionIdArg) {
		return sessionIdArg;
	}
	
	public static TestRunnerArgs parse(String... args) {
		TestRunnerArgs testRunnerArgs = new TestRunnerArgs();
		HashMap<String, String> argValues = null;

		argValues = getArgValues(args);

		if (argValues == null || !argValues.containsKey(ARG_TEST_BUNDLE_IDS)) {
			logger.info("Argument '{}' is missing", ARG_TEST_BUNDLE_IDS);
			return null;
		}

		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();

			sb.append("\n---------ARGS---------\n");
			for (var key : argValues.keySet())
				sb.append(String.format("[%s:%s]\n", key, argValues.get(key)));
			sb.append("\n---------------------------\n");

			logger.debug(sb.toString());
		}

		testRunnerArgs.testBundles = getTestBundles(argValues.get(ARG_TEST_BUNDLE_IDS));
		testRunnerArgs.resolutionMode = getResolutionMode(argValues.get(ARG_TEST_BUNDLE_MODE));
		testRunnerArgs.callbackUrl = getCallbackUrl(argValues.get(ARG_CALLBACK_URL));
		testRunnerArgs.sessionId = getSessionId(argValues.get(ARG_SESSION_ID));
		
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\n--------TEST BUNDLES---------");
			for (var testBundle : testRunnerArgs.testBundles)
				sb.append("\n" + testBundle.toString());
			sb.append("\n---------------------------\n");
			logger.debug(sb.toString());

		}
		return testRunnerArgs;
	}
}
