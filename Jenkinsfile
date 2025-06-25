pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'changes', url: 'https://github.com/DatlaBharath/HelloService'
            }
        }
        
        stage('Vulnerability Scan') {
            steps {
                script {
                    // Step 1: Perform curl request to scan
                    def response = sh(script: """
                        curl --location \"http://microservice-genai.uksouth.cloudapp.azure.com/api/vmsb/pipelines/initscan\" \
                        --header \"Content-Type: application/json\" \
                        --data '{ 
                            \"encrypted_user_id\": \"gAAAAABn0rtiUIre85Q28N4qZj7Ks30nAI8gukwzyeAengetWJ4CbZzfyQbgpP6wFXrXm0BROOwL4ps-uefe8pmcPDeergw7SA==\",
                            \"scanner_id\": 1,
                            \"target_branch\": \"changes\", 
                            \"repo_url\": \"https://github.com/DatlaBharath/HelloService\",
                            \"pat\": \"${PAT}\"
                        }'
                    """, returnStdout: true).trim()
                    
                    echo "Curl response: ${response}"

                    // Escape the response
                    def escapedResponse = sh(script: "echo '${response}' | sed 's/\"/\\\\\"/g'", returnStdout: true).trim()

                    // Step 2: Send the escaped response to save it
                    def sessionId = "your_session_id" // adjust this logic if necessary
                    sh """
                        curl -X POST http://ec2-13-201-18-57.ap-south-1.compute.amazonaws.com/app/save-curl-response-jenkins?sessionId=\${encodeURIComponent('${sessionId}')} \
                        -H "Content-Type: application/json" \
                        -d "{\\"response\\": \\"${escapedResponse}\\"}"
                    """
                }
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
                    def imageName = "ratneshpuskar/helloservice:${env.BUILD_NUMBER}"

                    // Build Docker image
                    sh "docker build -t ${imageName} ."

                    // Push Docker image
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh 'echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin'
                        sh "docker push ${imageName}"
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
                            image: ratneshpuskar/helloservice:${env.BUILD_NUMBER}
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

                    sh """echo "${deploymentYaml}" > deployment.yaml"""
                    sh """echo "${serviceYaml}" > service.yaml"""

                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.111.171.100 "kubectl apply -f -" < deployment.yaml'
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.111.171.100 "kubectl apply -f -" < service.yaml'
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