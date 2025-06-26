pipeline {
    agent any
    stages {
        stage('Git Clone') {
            steps {
                git 'https://github.com/DatlaBharath/HelloService'
            }
        }
        
        stage('Build') {
            steps {
                script {
                    def mvnHome = tool 'Maven'
                    sh "${mvnHome}/bin/mvn clean package"
                }
            }
        }
        
        stage('Docker Build and Push') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            docker build -t ratneshpuskar/hello-service:${env.BUILD_NUMBER} .
                            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                            docker push ratneshpuskar/hello-service:${env.BUILD_NUMBER}
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh """
                        kubectl set image deployment/hello-service hello-service=ratneshpuskar/hello-service:${env.BUILD_NUMBER} --record
                        kubectl rollout status deployment/hello-service
                    """
                }
            }
        }
        
        stage('Post-Deployment') {
            steps {
                echo 'Deployment Completed!'
            }
        }
    }
}