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
        stage('Build Docker Image') {
            steps {
                script {
                    def IMAGE = "ratneshpuskar/${env.GIT_URL.split('/').last().tokenize('.').first().toLowerCase()}:${env.BUILD_NUMBER}"
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                        sh 'echo "$PASS" | docker login -u $USER --password-stdin'
                        sh "docker build -t ${IMAGE} ."
                        sh "docker push ${IMAGE}"
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def deployYaml = """
                    apiVersion: apps/v1
                    kind: Deployment
                    metadata:
                      name: hello-deployment
                    spec:
                      replicas: 1
                      selector:
                        matchLabels:
                          app: hello
                      template:
                        metadata:
                          labels:
                            app: hello
                        spec:
                          containers:
                          - name: hello-service
                            image: ratneshpuskar/${env.GIT_URL.split('/').last().tokenize('.').first().toLowerCase()}:${env.BUILD_NUMBER}
                            ports:
                            - containerPort: 5000
                    """
                    def serviceYaml = """
                    apiVersion: v1
                    kind: Service
                    metadata:
                      name: hello-service
                    spec:
                      selector:
                        app: hello
                      ports:
                      - protocol: TCP
                        port: 5000
                        targetPort: 5000
                        nodePort: 30007
                      type: NodePort
                    """
                    sh """
                    echo "${deployYaml}" > deployment.yaml
                    echo "${serviceYaml}" > service.yaml
                    ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.110.127.144 "kubectl apply -f -" < deployment.yaml
                    ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.110.127.144 "kubectl apply -f -" < service.yaml
                    """
                }
            }
        }
    }
    post {
        success {
            echo 'Deployment was successful.'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}