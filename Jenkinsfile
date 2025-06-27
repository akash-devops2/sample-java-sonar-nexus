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
        FILE = "target/sample-java-app-${VERSION}.jar"
        IMAGE_NAME = 'sample-java-app'
        IMAGE_TAR = "${IMAGE_NAME}-${VERSION}.tar"
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

        stage('Upload JAR to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh '''
                        curl -v -u $USERNAME:$PASSWORD --upload-file $FILE \
                        $NEXUS_URL/repository/$REPO/$(echo $GROUP_ID | tr '.' '/')/$ARTIFACT_ID/$VERSION/$ARTIFACT_ID-$VERSION.$PACKAGING
                    '''
                }
            }
        }

        stage('Docker Build & Upload Image Tar to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    script {
                        // Create Dockerfile dynamically
                        writeFile file: 'Dockerfile', text: """
                            FROM openjdk:17
                            WORKDIR /app
                            COPY ${FILE} app.jar
                            ENTRYPOINT ["java", "-jar", "app.jar"]
                        """

                        // Build image and save as tar
                        sh """
                            docker build -t ${IMAGE_NAME}:${VERSION} .
                            docker save ${IMAGE_NAME}:${VERSION} -o ${IMAGE_TAR}
                        """

                        // Upload tar to Nexus raw repo
                        sh """
                            curl -v -u $USERNAME:$PASSWORD --upload-file ${IMAGE_TAR} \
                            ${NEXUS_URL}/repository/docker-image-raw/${IMAGE_TAR}
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
