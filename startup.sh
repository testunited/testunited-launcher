#!/bin/bash

java -cp .:/app/app.jar org.testunited.launcher.TestArtifactManager $*
java -cp .:/app/test-jars/*:/app/app.jar org.testunited.launcher.TestUnitedTestLauncher $*