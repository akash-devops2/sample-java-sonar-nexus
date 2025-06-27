pipeline {
    agent any

    environment {
        SONAR_HOST_URL     = 'http://13.235.82.221:30900/'
        NEXUS_URL          = 'http://13.235.82.221:30001'
        REPO               = 'maven-releases'
        GROUP_ID           = 'com.devops'
        ARTIFACT_ID        = 'sample-java-app'
        PACKAGING          = 'jar'
        DOCKER_IMAGE_NAME  = 'sample-java-app'
        DOCKER_REGISTRY    = '13.235.82.221:30002' // Nexus Docker port
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/akash-devops2/sample-java-sonar-nexus.git', branch: 'main'
            }
        }

        stage('Auto-Increment Version') {
            steps {
                script {
                    def currentVersion = readFile('version.txt').trim()
                    def parts = currentVersion.tokenize('.')
                    def major = parts[0].toInteger()
                    def minor = parts[1].toInteger() + 1
                    def newVersion = "${major}.${minor}"
                    echo "🔁 New Build Version: ${newVersion}"

                    // Save & push version
                    writeFile file: 'version.txt', text: newVersion
                    sh '''
                        git config user.email "jenkins@example.com"
                        git config user.name "Jenkins"
                        git add version.txt
                        git commit -m "🔁 Bumped version to ${newVersion}" || true
                        git push origin main || true
                    '''

                    env.VERSION = newVersion
                    env.FILE = "target/${ARTIFACT_ID}-${newVersion}.jar"
                }
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

                        // Generate Dockerfile
                        writeFile file: 'Dockerfile', text: """
                        FROM openjdk:17
                        WORKDIR /app
                        COPY ${FILE} app.jar
                        ENTRYPOINT ["java", "-jar", "app.jar"]
                        """

                        sh "docker build -t ${imageTag} ."

                        // Push to Nexus Docker Registry
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
