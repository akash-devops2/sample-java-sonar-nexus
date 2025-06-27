pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://13.235.82.221:30900'
        SONAR_PROJECT_KEY = 'sample-java-app'
        ARTIFACT_ID = 'sample-java-app'
        GROUP_ID = 'com.devops'
        PACKAGING = 'jar'
        REPO = 'maven-releases'
        NEXUS_URL = 'http://your-nexus-url:8081'
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
                    APP_VERSION = sh(script: "jx-release-version -next-version=increment:patch", returnStdout: true).trim()
                    echo "Version for this build: ${APP_VERSION}"
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('MySonar') {
                        sh """
                            mvn clean verify sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_TOKEN} \
                            -Dsonar.projectVersion=${APP_VERSION}
                        """
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh "mvn versions:set -DnewVersion=${APP_VERSION}"
                sh "mvn clean package"
            }
        }

        stage('Upload to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    script {
                        def groupPath = GROUP_ID.replace('.', '/')
                        def fileName = "${ARTIFACT_ID}-${APP_VERSION}.${PACKAGING}"
                        def filePath = "target/${fileName}"
                        
                        sh """
                            echo "Uploading file: ${filePath}"
                            test -f ${filePath} || (echo "❌ JAR file not found: ${filePath}" && exit 1)

                            curl -v -u ${USERNAME}:${PASSWORD} --upload-file ${filePath} \\
                            ${NEXUS_URL}/repository/${REPO}/${groupPath}/${ARTIFACT_ID}/${APP_VERSION}/${fileName}
                        """
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    def imageTag = "${APP_VERSION}"
                    sh """
                        docker build -t your-dockerhub-username/${ARTIFACT_ID}:${imageTag} .
                        docker push your-dockerhub-username/${ARTIFACT_ID}:${imageTag}
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build successful for version ${APP_VERSION}"
        }
        failure {
            echo "❌ Build failed for version ${APP_VERSION}"
        }
    }
}
