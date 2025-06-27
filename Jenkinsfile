pipeline {
    agent any

    environment {
        SONAR_HOST_URL     = 'http://13.235.82.221:30900/'
        NEXUS_URL          = 'http://13.235.82.221:30001'
        REPO               = 'maven-releases'
        GROUP_ID           = 'com.devops'
        ARTIFACT_ID        = 'sample-java-app'
        VERSION            = '1.1.2'
        PACKAGING          = 'jar'
        FILE               = "target/sample-java-app-1.1.2.jar"
        DOCKER_IMAGE_NAME  = 'sample-java-app'
        DOCKER_REGISTRY    = '13.235.82.221:30002'  // Nexus Docker hosted port
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

        stage('Upload to Nexus Maven Repo') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh '''
                        curl -v -u $USERNAME:$PASSWORD --upload-file $FILE \
                        $NEXUS_URL/repository/$REPO/$(echo $GROUP_ID | tr '.' '/')/$ARTIFACT_ID/$VERSION/$ARTIFACT_ID-$VERSION.$PACKAGING
                    '''
                }
            }
        }

        stage('Build & Push Docker Image to Nexus Docker Registry') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-docker-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    script {
                        def imageTag = "${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${VERSION}"

                        // Create Dockerfile
                        writeFile file: 'Dockerfile', text: """
                        FROM openjdk:17
                        WORKDIR /app
                        COPY ${FILE} app.jar
                        ENTRYPOINT ["java", "-jar", "app.jar"]
                        """

                        // Build Docker image
                        sh """
                            docker build -t ${imageTag} .
                        """

                        // Login to Nexus Docker Registry & Push image
                        sh """
                            echo "$DOCKER_PASS" | docker login ${DOCKER_REGISTRY} -u "$DOCKER_USER" --password-stdin
                            docker push ${imageTag}
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
