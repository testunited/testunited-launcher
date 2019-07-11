pipeline {
    agent any 
    environment {
        REGISTRY="registry.minikube.local:80"
        DOCKER_IMAGE_LOCAL="testunited/testunited-launcher"
        DOCKER_IMAGE_REMOTE="${REGISTRY}/${DOCKER_IMAGE_LOCAL}:latest"
    }
    stages {
        stage('Build') { 
            steps {
                sh "gradle build -x test"
            }
        }
        stage('Dev Test') { 
            steps {
                sh "gradle test"
            }
        }
        stage('Package') { 
            steps {
                sh "gradle jar docker"
            }
        }
        stage('Publish') { 
            steps {
                sh "gradle publish publishToMavenLocal"
                sh "docker tag ${DOCKER_IMAGE_LOCAL} ${DOCKER_IMAGE_REMOTE}"
                sh "docker push ${DOCKER_IMAGE_REMOTE}"
            }
        }
    }
}