package org.testunited.launcher;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleRepositoryEventListener extends AbstractRepositoryListener {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void artifactInstalled(RepositoryEvent event) {
		logger.info("artifact {} installed to file {}\n", event.getArtifact(), event.getFile());
	}

	@Override
	public void artifactInstalling(RepositoryEvent event) {
		logger.info("installing artifact {} to file {}\n", event.getArtifact(), event.getFile());
	}

	@Override
	public void artifactResolved(RepositoryEvent event) {
		logger.info("artifact {} resolved from repository {}\n", event.getArtifact(), event.getRepository());
	}

	@Override
	public void artifactDownloading(RepositoryEvent event) {
		logger.info("downloading artifact {} from repository {}\n", event.getArtifact(), event.getRepository());
	}

	@Override
	public void artifactDownloaded(RepositoryEvent event) {
		logger.info("downloaded artifact {} from repository {}\n", event.getArtifact(), event.getRepository());
	}

	@Override
	public void artifactResolving(RepositoryEvent event) {
		logger.info("resolving artifact {}\n", event.getArtifact());
	}

}
