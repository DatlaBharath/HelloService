pipeline {
    agent any 
    environment {
        DOCKER_REGISTRY = "your-docker-registry"
        KUBECONFIG_CRED_ID = 'your-kubeconfig-credential-id'
        DOCKER_CRED_ID = 'your-docker-credential-id'
        MAVEN_PATH = '/path/to/mvn'
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    def scmVars = checkout scm
                }
            }
        }
        
        stage('Build') {
            steps {
                sh "${MAVEN_PATH}/mvn clean package"
            }
        }
        
        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: DOCKER_CRED_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin "${DOCKER_REGISTRY}"
                        docker build -t ${DOCKER_REGISTRY}/your-image-name:${env.BUILD_NUMBER} .
                        docker push ${DOCKER_REGISTRY}/your-image-name:${env.BUILD_NUMBER}
                    """
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([file(credentialsId: KUBECONFIG_CRED_ID, variable: 'KUBECONFIG')]) {
                    sh """
                        kubectl --kubeconfig=\${KUBECONFIG} set image deployment/your-deployment your-container=${DOCKER_REGISTRY}/your-image-name:${env.BUILD_NUMBER}
                        kubectl --kubeconfig=\${KUBECONFIG} rollout status deployment/your-deployment
                    """
                }
            }
        }
    }
}