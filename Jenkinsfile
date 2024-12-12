pipeline {
    agent any

    tools {
        nodejs "nodejs_tool_name"
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: "main", url: "your_repo_url"
            }
        }

        stage('Build Project') {
            steps {
                sh 'npm install --no-audit --no-fund --no-optional'
                sh 'npm run build --if-present'
            }
        }

        stage('Create Docker Image') {
            steps {
                script {
                    def repoName = "github_repo_name_in_lowercase"
                    def buildNumber = env.BUILD_NUMBER
                    def imageName = "sakthi/${repoName}:${buildNumber}"
                    sh "docker build -t ${imageName} ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'sajthi', passwordVariable: 'DOCKER_HUB_PASSWORD', usernameVariable: 'DOCKER_HUB_USERNAME')]) {
                    sh 'echo "${DOCKER_HUB_PASSWORD}" | docker login -u "${DOCKER_HUB_USERNAME}" --password-stdin'
                    def imageName = "sakthi/github_repo_name_in_lowercase:${env.BUILD_NUMBER}"
                    sh "docker push ${imageName}"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                writeFile file: 'deployment.yaml', text: """
                    apiVersion: apps/v1
                    kind: Deployment
                    metadata:
                      name: project-deployment
                      labels:
                        app: project
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
                          - name: project-container
                            image: sakthi/github_repo_name_in_lowercase:${env.BUILD_NUMBER}
                            ports:
                            - containerPort: 8080
                """
                
                writeFile file: 'service.yaml', text: """
                    apiVersion: v1
                    kind: Service
                    metadata:
                      name: project-service
                    spec:
                      type: NodePort
                      selector:
                        app: project
                      ports:
                      - protocol: "TCP"
                        port: 80
                        targetPort: 8080
                        nodePort: 30007
                """
                
                sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@your_kubernetes_server "kubectl apply -f -" < deployment.yaml'
                sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@your_kubernetes_server "kubectl apply -f -" < service.yaml'
            }
        }
    }

    post {
        success {
            echo "Build and deployment completed successfully!"
        }
        
        failure {
            echo "Build or deployment failed."
        }
    }
}