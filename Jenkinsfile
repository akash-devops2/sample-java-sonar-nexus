pipeline {
    agent any

    environment {
        SONAR_HOST_URL          = 'http://13.235.82.221:30900'
        NEXUS_URL               = 'http://13.235.82.221:30001'
        NEXUS_REPO              = 'maven-releases'
        GROUP_ID                = 'com.devops'
        ARTIFACT_ID             = 'sample-java-app'
        BUILD_OFFSET            = '44' // Adjust this offset to get 1.0 now (i.e., 43rd build = 1.0)
        VERSION                 = "1.${BUILD_NUMBER.toInteger() - BUILD_OFFSET.toInteger()}"
        FILE_NAME               = "sample-java-app-${VERSION}.jar"
        DOCKER_IMAGE_NAME       = 'sample-java-app'
        NEXUS_DOCKER_REGISTRY   = '13.235.82.221:30002'
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
                            -Dsonar.projectVersion=$VERSION \
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
                sh 'ls -l target/'
            }
        }

        stage('Upload to Nexus Maven Repo') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh '''
                        curl -v -u $USERNAME:$PASSWORD --upload-file target/sample-java-app.jar \
                        $NEXUS_URL/repository/$NEXUS_REPO/$(echo $GROUP_ID | tr '.' '/')/$ARTIFACT_ID/$VERSION/$FILE_NAME
                    '''
                }
            }
        }

        stage('Build & Push Docker Image to Nexus Docker Registry') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-docker-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    script {
                        def imageTag = "${NEXUS_DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${VERSION}"

                        // Write Dockerfile
                        writeFile file: 'Dockerfile', text: """
                        FROM openjdk:17
                        WORKDIR /app
                        COPY target/sample-java-app.jar app.jar
                        ENTRYPOINT ["java", "-jar", "app.jar"]
                        """

                        // Build and Push
                        sh """
                            docker build -t ${imageTag} .
                            echo "$DOCKER_PASS" | docker login ${NEXUS_DOCKER_REGISTRY} -u "$DOCKER_USER" --password-stdin
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
