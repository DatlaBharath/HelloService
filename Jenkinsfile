pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'myapp-image'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        KUBECONFIG_CREDENTIALS_ID = 'kubeconfig-credentials'
        SSH_CREDENTIALS_ID = 'ssh-credentials'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                script {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${env.BUILD_ID}")
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: DOCKER_CREDENTIALS_ID,
                        usernameVariable: 'USERNAME',
                        passwordVariable: 'PASSWORD'
                    )]) {
                        sh """
                            echo "${PASSWORD}" | docker login -u "${USERNAME}" --password-stdin
                            docker push "${DOCKER_IMAGE}:${env.BUILD_ID}"
                        """
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    withCredentials([file(credentialsId: KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_FILE')]) {
                        withEnv(["KUBECONFIG=${KUBECONFIG_FILE}"]) {
                            sh """
                                kubectl set image deployment/myapp myapp="${DOCKER_IMAGE}:${env.BUILD_ID}"
                                kubectl rollout status deployment/myapp
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                cleanWs()
            }
        }
    }
}