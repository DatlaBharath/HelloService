pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: "https://github.com/DatlaBharath/HelloService"
            }
        }

        stage('Curl Request') {
            steps {
                script {
                    def response = sh(script: '''
                        curl --location "http://microservice-genai.uksouth.cloudapp.azure.com/api/vmsb/pipelines/initscan" \
                        --header "Content-Type: application/json" \
                        --data '{
                            "encrypted_user_id": "gAAAAABn0rtiUIre85Q28N4qZj7Ks30nAI8gukwzyeAengetWJ4CbZzfyQbgpP6wFXrXm0BROOwL4ps-uefe8pmcPDeergw7SA==",
                            "scanner_id": 1,
                            "target_branch": "main", 
                            "repo_url": "https://github.com/DatlaBharath/HelloService",
                            "pat": "${PAT}"
                        }'
                    ''', returnStdout: true).trim()

                    echo "Curl response: ${response}"

                    def escapedResponse = sh(script: "echo '${response}' | sed 's/\"/\\\\\"/g'", returnStdout: true).trim()

                    def jsonData = "{\"response\": \"${escapedResponse}\"}"

                    def contentLength = jsonData.length()

                    sh """
                    curl -X POST http://ec2-13-201-18-57.ap-south-1.compute.amazonaws.com/app/save-curl-response-jenkins?sessionId=${encodeURIComponent((sessionId))} \
                    -H "Content-Type: application/json" \
                    -d '${jsonData}'
                    """
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
                    def imageName = "ratneshpuskar/helloservice-jenkins:${env.BUILD_NUMBER}".toLowerCase()
                    sh "docker build -t ${imageName} ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh 'echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin'
                        def imageName = "ratneshpuskar/helloservice-jenkins:${env.BUILD_NUMBER}".toLowerCase()
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

                    sh 'scp -i /var/test.pem deployment.yaml ubuntu@15.206.166.65:~/'
                    sh 'scp -i /var/test.pem service.yaml ubuntu@15.206.166.65:~/'
                    sh 'ssh -i /var/test.pem ubuntu@15.206.166.65 "kubectl apply -f ~/deployment.yaml"'
                    sh 'ssh -i /var/test.pem ubuntu@15.206.166.65 "kubectl apply -f ~/service.yaml"'
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