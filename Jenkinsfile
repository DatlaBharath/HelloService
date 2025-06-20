pipeline {
    agent any
    tools {
        maven 'Maven'
    }
    environment {
        PAT = credentials('pat-key')
        DOCKER_USERNAME = credentials('dockerhub_credentials')
        DOCKER_PASSWORD = credentials('dockerhub_credentials')
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/DatlaBharath/HelloService'
            }
        }
        stage('Curl Request') {
            steps {
                script {
                    def response = sh(script: """
                        curl --location "http://microservice-genai.uksouth.cloudapp.azure.com/api/vmsb/pipelines/initscan" \
                        --header "Content-Type: application/json" \
                        --data '{
                            "encrypted_user_id": "gAAAAABn0rtiUIre85Q28N4qZj7Ks30nAI8gukwzyeAengetWJ4CbZzfyQbgpP6wFXrXm0BROOwL4ps-uefe8pmcPDeergw7SA==",
                            "scanner_id": 1,
                            "target_branch": "main", 
                            "repo_url": "https://github.com/DatlaBharath/HelloService",
                            "pat": "${PAT}"
                        }'
                    """, returnStdout: true).trim()
                    
                    def escapedResponse = response.replaceAll('"', '\\"')
                    def jsonData = "{\"response\": \"${escapedResponse}\"}"
                    sh """
                    curl -X POST "http://ec2-13-201-18-57.ap-south-1.compute.amazonaws.com/app/save-curl-response-jenkins?sessionId=\${env.BUILD_ID}" \
                    -H "Content-Type: application/json" \
                    -d '${jsonData}'
                    """
                    
                    def vulnerabilities = readJSON(text: response)
                    if (vulnerabilities.high + vulnerabilities.medium > 0) {
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
                    def imageName = "ratneshpuskar/${env.JOB_NAME.toLowerCase()}:${env.BUILD_NUMBER}"
                    sh "docker build -t ${imageName} ."
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh 'echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin'
                        def imageName = "ratneshpuskar/${env.JOB_NAME.toLowerCase()}:${env.BUILD_NUMBER}"
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
                      name: helloservice
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
                            image: ratneshpuskar/${env.JOB_NAME.toLowerCase()}:${env.BUILD_NUMBER}
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
                        port: 5000
                        targetPort: 5000
                      type: NodePort
                    """
                    
                    sh "echo \"${deploymentYaml}\" > deployment.yaml"
                    sh "echo \"${serviceYaml}\" > service.yaml"
                    
                    sh 'scp -i /var/test.pem -o StrictHostKeyChecking=no deployment.yaml ubuntu@13.201.36.176:/tmp/deployment.yaml'
                    sh 'scp -i /var/test.pem -o StrictHostKeyChecking=no service.yaml ubuntu@13.201.36.176:/tmp/service.yaml'
                    
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@13.201.36.176 "kubectl apply -f /tmp/deployment.yaml"'
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@13.201.36.176 "kubectl apply -f /tmp/service.yaml"'
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