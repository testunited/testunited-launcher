package org.testunited.launcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

public class TestArtifactManager {
	public static final String DEFAULT_LOCAL_REPOSITORY = "m2";
	public static final String DEFAULT_LOCAL_REPOSITORY_CACHE = "m2-cache";
	public static final String DEFAULT_TEST_ARTIFACT_CACHE = "test-jars";
	private List<RemoteRepository> remoteRepos;
	private String m2LocalHome;
	private TestBundleResolutionMode testBundleResolutionMode;

	public static void main(String[] args) {

		TestRunnerArgs testRunnerArgs = TestRunnerArgs.parse(args);

		var artifactManager = new TestArtifactManager(testRunnerArgs.resolutionMode);
		artifactManager.m2LocalHome = DEFAULT_LOCAL_REPOSITORY;
		artifactManager.addRemoteRepository("deps", "default", "https://repo.deps.co/chamithsri/snapshots", 
				"DEPS6HOT54LRQDKB2EYR", "xIMkoIxBZUyA355BMp6VoD5J7406E9ROM_n7KhEo");
		artifactManager.resolveTestBundles(testRunnerArgs.testBundles);

	}

	public TestArtifactManager() {
		this(TestBundleResolutionMode.Classpath);
	}

	public TestArtifactManager(TestBundleResolutionMode testBundleResolutionMode) {
		this.testBundleResolutionMode = testBundleResolutionMode;
		this.remoteRepos = new ArrayList<RemoteRepository>();
	}

	public void resolveTestBundle(TestBundle testBundle) {

		if (this.testBundleResolutionMode == TestBundleResolutionMode.Classpath)
			return;

		RepositorySystem repositorySystem = getRepositorySystem();
		RepositorySystemSession repositorySystemSession = getRepositorySystemSession(repositorySystem);

		Artifact artifact = new DefaultArtifact(testBundle.group, testBundle.artifact, "jar", testBundle.version);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		artifactRequest.setRepositories(getRepositories(repositorySystem, repositorySystemSession));

		try {
			ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);
			artifact = artifactResult.getArtifact();
			System.out.printf("artifact %s resolved to %s\n", artifact, artifact.getFile());

			this.copyToCache(artifact);

		} catch (Exception e) {
			System.err.printf("error resolving artifact: %s\n", e.getMessage());
		}
	}

	public void addRemoteRepository(String name, String type, String url, String userName, String password) {
		Authentication auth = new AuthenticationBuilder().addUsername(userName).addPassword(password).build();
        RemoteRepository nexus =
            new RemoteRepository.Builder(name, type, url ).setAuthentication( auth ).build();
        this.remoteRepos.add(nexus);
	}
	
	public void resolveTestBundles(List<TestBundle> testBundles) {
		for (var tb : testBundles) {
			this.resolveTestBundle(tb);
		}
	}

	public void copyToCache(Artifact artifact) {
		Path original = Paths.get(artifact.getFile().getAbsolutePath());
		Path copied = Paths.get(
				FileSystems.getDefault().getPath(DEFAULT_TEST_ARTIFACT_CACHE).toAbsolutePath().toString(),
				artifact.getFile().getName());

		try {
			if (!Files.exists(copied))
				Files.createSymbolicLink(copied, original);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public RepositorySystem getRepositorySystem() {
		DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
		serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		serviceLocator.addService(TransporterFactory.class, FileTransporterFactory.class);
		serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		serviceLocator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
			@Override
			public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
				System.err.printf("error creating service: %s\n", exception.getMessage());
				exception.printStackTrace();
			}
		});

		return serviceLocator.getService(RepositorySystem.class);
	}

	public DefaultRepositorySystemSession getRepositorySystemSession(RepositorySystem system) {
		DefaultRepositorySystemSession repositorySystemSession = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepository = new LocalRepository(DEFAULT_LOCAL_REPOSITORY_CACHE);
		repositorySystemSession
				.setLocalRepositoryManager(system.newLocalRepositoryManager(repositorySystemSession, localRepository));

		repositorySystemSession.setRepositoryListener(new ConsoleRepositoryEventListener());

		return repositorySystemSession;
	}

	public List<RemoteRepository> getRepositories(RepositorySystem system, RepositorySystemSession session) {
		List<RemoteRepository> repos = new ArrayList<RemoteRepository>();
		if(this.testBundleResolutionMode == TestBundleResolutionMode.Local)
			repos.add(getLocalMavenRepository());
		else if (this.testBundleResolutionMode == TestBundleResolutionMode.Remote)
			repos.addAll(remoteRepos);
		return repos;
	}

	private RemoteRepository getCentralMavenRepository() {
		return new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/").build();
	}

	private RemoteRepository getLocalMavenRepository() {
		Path m2LocalHomeAbsolutePath = Paths.get(this.m2LocalHome, "repository").toAbsolutePath();
		return new RemoteRepository.Builder("local", "default", "file:" + m2LocalHomeAbsolutePath.toString()).build();
	}
}
