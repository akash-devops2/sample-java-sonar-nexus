pipeline {
    agent any

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

        stage('Generate Version') {
            steps {
                script {
                    def version = sh(script: "jx-release-version -dir ${WORKSPACE} -next-version=increment:patch", returnStdout: true).trim()
                    env.APP_VERSION = version
                    echo "Version for this build: ${env.APP_VERSION}"
                }
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
                              -Dsonar.projectVersion=${APP_VERSION}
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
                        $NEXUS_URL/repository/$REPO/\$GROUP_PATH/$ARTIFACT_ID/${APP_VERSION}/$ARTIFACT_ID-${APP_VERSION}.$PACKAGING
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build successful with version ${APP_VERSION}"
        }
        failure {
            echo "❌ Build failed for version ${APP_VERSION}"
        }
    }
}
