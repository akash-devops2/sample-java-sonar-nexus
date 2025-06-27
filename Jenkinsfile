pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://13.235.82.221:30900/'
        NEXUS_URL = 'http://13.235.82.221:30801'
        REPO = 'maven-releases'
        GROUP_ID = 'com.devops'
        ARTIFACT_ID = 'sample-java-app'
        PACKAGING = 'jar'
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
                echo "🔧 Docker Build & Push stage here (to be implemented)..."
                // Add your Docker steps here later
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
