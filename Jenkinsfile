pipeline {
    agent any
    tools {
        maven 'Maven'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/main']], userRemoteConfigs: [[url: 'https://github.com/DatlaBharath/HelloService']]])
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def dockerImage = "ratneshpuskar/helloservice:${env.BUILD_NUMBER}"
                    sh "docker build -t ${dockerImage} ."
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh 'echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin'
                    sh "docker push ratneshpuskar/helloservice:${env.BUILD_NUMBER}"
                }
            }
        }
        stage('Create Deployment YAML') {
            steps {
                script {
                    def deploymentYaml = """
                    apiVersion: apps/v1
                    kind: Deployment
                    metadata:
                      name: hello-service-deployment
                      labels:
                        app: hello-service
                    spec:
                      replicas: 1
                      selector:
                        matchLabels:
                          app: hello-service
                      template:
                        metadata:
                          labels:
                            app: hello-service
                        spec:
                          containers:
                          - name: hello-service
                            image: ratneshpuskar/helloservice:${env.BUILD_NUMBER}
                            ports:
                            - containerPort: 5000
                    """
                    writeFile file: 'deployment.yaml', text: deploymentYaml
                }
            }
        }
        stage('Create Service YAML') {
            steps {
                script {
                    def serviceYaml = """
                    apiVersion: v1
                    kind: Service
                    metadata:
                      name: hello-service
                    spec:
                      type: NodePort
                      selector:
                        app: hello-service
                      ports:
                        - port: 5000
                          nodePort: 30007
                    """
                    writeFile file: 'service.yaml', text: serviceYaml
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                sshagent (credentials: ['k8s-instance-ssh']) {
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.109.144.79 "kubectl apply -f -" < deployment.yaml'
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.109.144.79 "kubectl apply -f -" < service.yaml'
                }
            }
        }
    }
    post {
        success {
            echo 'Deployment completed successfully.'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}