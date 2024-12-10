pipeline {
    agent any
    tools { ["Tool Name"] }
    stages {
        stage('Checkout') {
            steps {
                ["Checkout Command"]
            }
        }
        stage('Build Image') {
            steps {
                sh '[" Build Command"] -DskipTests'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def imageName = "ratneshpuskar/[github_repo_name].toLowerCase():${env.BUILD_NUMBER}"
                    sh "docker build -t ${imageName} ."
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUsername')]) {
                    sh 'echo "${dockerHubPassword}" | docker login -u "${dockerHubUsername}" --password-stdin'
                    sh 'docker push "ratneshpuskar/[github_repo_name].toLowerCase():${env.BUILD_NUMBER}"'
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
  name: project-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: project
  template:
    metadata:
      labels:
        app: project
    spec:
      containers:
      - name: project
        image: ratneshpuskar/[github_repo_name].toLowerCase():${env.BUILD_NUMBER}
        ports:
        - containerPort:  "Docker_Container_Port"
"""
                    def serviceYaml = """
apiVersion: v1
kind: Service
metadata:
  name: project-service
spec:
  type: NodePort
  ports:
  - port: "Walking_Cat_Port"
    targetPort: "Docker_Container_Port"
    nodePort: 30007
  selector:
    app: project 
"""
                    sh 'echo "${deploymentYaml}" | ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@1.3.4.5 "kubectl apply -f -" < -'
                    sh 'echo "${serviceYaml}" | ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@1.3.4.5 "kubectl apply -f -" < -'
                }
            }
        }
    }
    post {
        success {
            echo 'Deployment successful.'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}