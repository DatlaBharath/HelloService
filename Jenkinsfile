pipeline {
    agent any
    tools {
        maven 'Maven'
    }
    stages {
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/DatlaBharath/HelloService', branch: 'main'
            }
        }
        stage('Build Maven Project') {
            steps {
                sh "mvn clean package -DskipTests"
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
                withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'DOCKER_HUB_PASS', usernameVariable: 'DOCKER_HUB_USER')]) {
                    sh "echo ${DOCKER_HUB_PASS} | docker login -u ${DOCKER_HUB_USER} --password-stdin"
                    sh "docker push ratneshpuskar/helloservice:${env.BUILD_NUMBER}"
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def deploymentYaml = """
                    apiVersion: apps/v1
                    kind: Deployment
                    metadata:
                      name: helloservice
                    spec:
                      replicas: 1
                      selector:
                        matchLabels:
                          app: helloservice
                      template:
                        metadata:
                          labels:
                            app: helloservice
                        spec:
                          containers:
                          - name: helloservice
                            image: ratneshpuskar/helloservice:${env.BUILD_NUMBER}
                            ports:
                            - containerPort: 5000
                    """
                    def serviceYaml = """
                    apiVersion: v1
                    kind: Service
                    metadata:
                      name: helloservice
                    spec:
                      selector:
                        app: helloservice
                      ports:
                      - protocol: TCP
                        port: 80
                        targetPort: 5000
                        nodePort: 30007
                      type: NodePort
                    """
                    writeFile(file: 'deployment.yaml', text: deploymentYaml)
                    writeFile(file: 'service.yaml', text: serviceYaml)
                    sh """
                    ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@13.233.113.41 "kubectl apply -f -" < deployment.yaml
                    ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@13.233.113.41 "kubectl apply -f -" < service.yaml
                    """
                }
            }
        }
    }
    post {
        success {
            echo 'The build and deployment were successful.'
        }
        failure {
            echo 'The build or deployment failed.'
        }
    }
}