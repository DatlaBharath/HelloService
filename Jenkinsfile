pipeline {
    agent any
    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/DatlaBharath/HelloService'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {
                   def awsAccountId = '522814716906'
                    def region = 'ap-south-1'
                    def repository = 'helloservice'
                    def imageName = "sakthisiddu1/helloservice:${env.BUILD_NUMBER}"
                    sh "docker build -t ${imageName} ."
                     sh "docker tag ${imageName} ${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${repository}:${env.BUILD_NUMBER}"
sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${awsAccountId}.dkr.ecr.${region}.amazonaws.com"
sh "docker push ${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${repository}:${env.BUILD_NUMBER}"
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
  name: helloservice-deployment
  labels:
    app: helloservice
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
        image: 522814716906.dkr.ecr.ap-south-1.amazonaws.com/helloservice:${env.BUILD_NUMBER}
        ports:
        - containerPort: 5000
"""

                    def serviceYaml = """
apiVersion: v1
kind: Service
metadata:
  name: helloservice-service
spec:
  selector:
    app: helloservice
  ports:
  - protocol: TCP
    port: 5000
    targetPort: 5000
    nodePort: 30007
  type: NodePort
"""

                    sh """echo "$deploymentYaml" > deployment.yaml"""
                    sh """echo "$serviceYaml" > service.yaml"""

                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.110.84.26 "kubectl apply -f -" < deployment.yaml'
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.110.84.26 "kubectl apply -f -" < service.yaml'
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment was successful'
        }
        failure {
            echo 'Deployment failed'
        }
    }
}