package org.testunited.launcher;

public class TestBundle {
String group, artifact, version, testPackage;

public TestBundle(String group, String artifact, String version, String testPackage) {
	super();
	this.group = group;
	this.artifact = artifact;
	this.version = version;
	this.testPackage = testPackage;
}

public String getGroup() {
	return group;
}

public void setGroup(String group) {
	this.group = group;
}

public String getArtifact() {
	return artifact;
}

public void setArtifact(String artifact) {
	this.artifact = artifact;
}

public String getVersion() {
	return version;
}

public void setVersion(String version) {
	this.version = version;
}

public String getTestPackage() {
	return testPackage;
}

public void setTestPackage(String testPackage) {
	this.testPackage = testPackage;
}

public String toString() {
	return String.format("{%s:%s:%s:%s}", this.group, this.artifact, this.version, this.testPackage);
}
}
