pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/DatlaBharath/HelloService-jenkins'
            }
        }
        stage('Curl Request') {
            steps {
                script {
                    // Capture the response from the curl request
                    def response = sh(script: """
                        curl --location "http://microservice-genai.uksouth.cloudapp.azure.com/api/vmsb/pipelines/initscan" \
                        --header "Content-Type: application/json" \
                        --data '{
                            "encrypted_user_id": "gAAAAABnyCdKTdqwwv1tgbx8CqlTQnyYbqWBATox1Q58q-y8PmXbXc4_65tTO3jRijx92hpZI1juGV-80apcQa0Z72HgzkJsiA==",
                            "scanner_id": 1,
                            "target_branch": "main", 
                            "repo_url": "https://github.com/DatlaBharath/HelloService",
                            "pat": "string"
                        }'
                    """, returnStdout: true).trim()
                    echo "Curl response: ${response}"

                    // Escape the response
                    def escapedResponse = sh(script: "echo '${response}' | sed 's/\"/\\\\\"/g'", returnStdout: true).trim()
                    def jsonData = "{\"response\": \"${escapedResponse}\"}"
                    def contentLength = jsonData.length()

                    // Send the response to your backend
                    sh """
                    curl -X POST http://ec2-13-201-18-57.ap-south-1.compute.amazonaws.com/app/save-curl-response-jenkins \
                    -H "Content-Type: application/json" \
                    -H "Content-Length: ${contentLength}" \
                    -d '${jsonData}'
                    """

                    // Check if the response contains 'success': true
                    def total_vulnerabilities = sh(script: "echo '${response}' | jq -r '.total_vulnerabilites'", returnStdout: true).trim()

                    try {
                        total_vulnerabilities = total_vulnerabilities.toInteger()
                    } catch (Exception e) {
                        echo "Warning: Could not parse total_vulnerabilities as integer: ${total_vulnerabilities}"
                        total_vulnerabilities = -1
                    }

                    if (total_vulnerabilities <= 0) {
                        echo "Success: No vulnerabilities found."
                        env.CURL_STATUS = 'true'
                    } else {
                        echo "Failure: Found ${total_vulnerabilities} vulnerabilities."
                        env.CURL_STATUS = 'false'
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
                    def imageName = "ratneshpuskar/helloservice-jenkins:${env.BUILD_NUMBER}"
                    sh "docker build -t ${imageName} ."
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh 'echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin'
                        def imageName = "ratneshpuskar/helloservice-jenkins:${env.BUILD_NUMBER}"
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
                            image: ratneshpuskar/helloservice-jenkins:${env.BUILD_NUMBER}
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

                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.6.238.137 "kubectl apply -f -" < deployment.yaml'
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.6.238.137 "kubectl apply -f -" < service.yaml'
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