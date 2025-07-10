pipeline {
    agent any

    environment {
        GIT_REPO = "https://github.com/DatlaBharath/HelloService"
        GIT_BRANCH = "main"
        DOCKER_CREDENTIAL_ID = "dockerhub_credentials"
        DOCKER_USERNAME = "sakthisiddu1"
        KUBERNETES_IP = "13.233.85.204"
        APP_PORT = "5000"
    }

    stages {
        stage('Clone Repository') {
            steps {
                git url: "${GIT_REPO}", branch: "${GIT_BRANCH}"
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def dockerImage = "${DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER}"
                    withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIAL_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh """
                            echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin
                            docker build -t $dockerImage .
                            docker push $dockerImage
                        """
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def dockerImage = "${DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER}"
                    sh """
                        kubectl set image deployment/your-deployment-name your-container-name=$dockerImage --record
                        kubectl expose deployment your-deployment-name --type=NodePort --port=${APP_PORT}
                    """
                }
            }
        }
    }
}