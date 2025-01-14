pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code from GitHub repository..."
                checkout([ $class: 'GitSCM', branches: [[name: "*/main"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: "https://github.com/DatlaBharath/HelloService"]] ])
            }
        }

        stage('Build') {
            steps {
                echo "Building the Maven project..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build and Push') {
            steps {
                script {
                    def dockerImageName = "ratneshpuskar/helloservice:${env.BUILD_NUMBER}"

                    echo "Building Docker image ${dockerImageName}..."
                    sh "docker build -t ${dockerImageName} ."

                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'echo $PASSWORD | docker login -u $USERNAME --password-stdin'
                        echo "Pushing Docker image to Docker Hub..."
                        sh "docker push ${dockerImageName}"
                    }
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
  name: hello-service-deployment
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

                    def serviceYaml = """
apiVersion: v1
kind: Service
metadata:
  name: hello-service-service
spec:
  type: NodePort
  ports:
  - port: 5000
    targetPort: 5000
    nodePort: 30007
  selector:
    app: hello-service
"""

                    writeFile file: 'deployment.yaml', text: deploymentYaml
                    writeFile file: 'service.yaml', text: serviceYaml

                    echo "Deploying to Kubernetes..."
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@65.2.179.84 "kubectl apply -f -" < deployment.yaml'
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@65.2.179.84 "kubectl apply -f -" < service.yaml'
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline executed successfully!"
        }
        failure {
            echo "Pipeline execution failed."
        }
    }
}