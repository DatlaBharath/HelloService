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
        
        stage('Check Vulnerabilities') {
            steps {
                script {
                    // Capture the response from the curl request
                    def response = sh(script: """
                        curl --location "http://microservice-genai.uksouth.cloudapp.azure.com/api/vmsb/pipelines/initscan" \\
                        --header "Content-Type: application/json" \\
                        --data '{
                            "encrypted_user_id": "gAAAAABn0rtiUIre85Q28N4qZj7Ks30nAI8gukwzyeAengetWJ4CbZzfyQbgpP6wFXrXm0BROOwL4ps-uefe8pmcPDeergw7SA==",
                             "scanner_id": 1,
                             "target_branch": "changes", 
                             "repo_url": "https://github.com/DatlaBharath/HelloService",
                             "pat": "pat-key"
                        }'
                    """, returnStdout: true).trim()
                    
                    echo "Curl response: ${response}"
                    
                    def escapedResponse = sh(script: "echo '${response}' | sed 's/\"/\\\\\"/g'", returnStdout: true).trim()
                    
                    // Send the response to the backend
                    sh """
                    curl -X POST http://ec2-13-201-18-57.ap-south-1.compute.amazonaws.com/app/save-curl-response-jenkins?sessionId=${encodeURIComponent((sessionId))} \\
                    -H "Content-Type: application/json" \\
                    -d "{\\"response\\": \\"${escapedResponse}\\"}"
                    """
                    
                    // Check if high or medium vulnerabilities exist
                    def total = sh(script: "echo '${response}' | jq -r '.total_vulnerabilites'", returnStdout: true).trim()
                    def high = sh(script: "echo '${response}' | jq -r '.high'", returnStdout: true).trim()
                    def medium = sh(script: "echo '${response}' | jq -r '.medium'", returnStdout: true).trim()
                    
                    try {
                        total = total.toInteger()
                        high = high.toInteger()
                        medium = medium.toInteger()
                    } catch (Exception e) {
                        echo "Warning: Could not parse vulnerability counts."
                        total = -1
                    }
                    
                    if (high + medium > 0) {
                        error("Vulnerabilities found, terminating pipeline.")
                    }
                }
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
                    def imageName = "ratneshpuskar/helloservice:${env.BUILD_NUMBER}"
                    sh "docker build -t ${imageName} ."
                }
            }
        }
        
        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh 'echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin'
                        def imageName = "ratneshpuskar/helloservice:${env.BUILD_NUMBER}"
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
                    
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@65.2.140.96 "kubectl apply -f -" < deployment.yaml'
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@65.2.140.96 "kubectl apply -f -" < service.yaml'
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