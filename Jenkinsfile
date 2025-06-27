pipeline {
    agent any

    parameters {
        string(name: 'APP_VERSION', defaultValue: '1.1.1', description: 'Application version (e.g., 1.1.1, 1.1.2)')
    }

    environment {
        SONAR_HOST_URL = 'http://13.235.82.221:30900/'
        NEXUS_URL = 'http://13.235.82.221:30801'
        REPO = 'maven-releases'
        GROUP_ID = 'com.devops'
        ARTIFACT_ID = 'sample-java-app'
        PACKAGING = 'jar'
        FILE = 'target/sample-java-app-1.0.jar'
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
                        sh """
                            mvn clean verify sonar:sonar \
                              -Dsonar.projectKey=sample-java-app \
                              -Dsonar.host.url=$SONAR_HOST_URL \
                              -Dsonar.login=$SONAR_TOKEN \
                              -Dsonar.projectVersion=${params.APP_VERSION}
                        """
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
                    sh """
                        GROUP_PATH=\$(echo $GROUP_ID | tr '.' '/')
                        curl -v -u \$USERNAME:\$PASSWORD --upload-file $FILE \\
                        $NEXUS_URL/repository/$REPO/\$GROUP_PATH/$ARTIFACT_ID/${params.APP_VERSION}/$ARTIFACT_ID-${params.APP_VERSION}.$PACKAGING
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for version ${params.APP_VERSION}!"
        }
        failure {
            echo "Pipeline failed for version ${params.APP_VERSION}!"
        }
    }
}
