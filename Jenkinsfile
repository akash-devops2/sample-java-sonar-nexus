pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://13.235.82.221:30900/'
        NEXUS_URL = 'http://13.235.82.221:30001'
        REPO = 'maven-releases'
        GROUP_ID = 'com.devops'
        ARTIFACT_ID = 'sample-java-app'
        VERSION = '1.1.2'
        PACKAGING = 'jar'
        FILE = 'target/sample-java-app-1.1.2.jar'
        DOCKER_REGISTRY = '13.235.82.221:30002'
        IMAGE_NAME = 'sample-java-app'
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/akash-devops2/sample-java-sonar-nexus.git', branch: 'main'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token-id', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('MySonar') {
                        sh '''
                            mvn clean verify sonar:sonar \
                              -Dsonar.projectKey=sample-java-app \
                              -Dsonar.host.url=$SONAR_HOST_URL \
                              -Dsonar.login=$SONAR_TOKEN
                        '''
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Upload to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh '''
