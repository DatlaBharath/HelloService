

    stages {
        stage('Checkout Code') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: "*/main"]], userRemoteConfigs: [[url: "https://github.com/your-repo.git"]]])
            }
        } 

        stage('Build Project') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            environment {
                DOCKER_CREDENTIALS = credentials('dockerhub_credentials')
            }
            steps {
                script {
                    def imageName = "ratneshpuskar/your-repo-name:${env.BUILD_NUMBER}".toLowerCase()
                    sh """
                        docker build -t ${imageName} .
                        echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin
                        docker push ${imageName}
                    """
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
                      name: your-deployment
                    spec:
                      replicas: 3
                      selector:
                        matchLabels:
                          app: your-app
                      template:
                        metadata:
                          labels:
                            app: your-app
                        spec:
                          containers:
                          - name: your-container
                            image: ratneshpuskar/your-repo-name:${env.BUILD_NUMBER}
                            ports:
                            - containerPort: 8080
                    """
                    def serviceYaml = """
                    apiVersion: v1
                    kind: Service
                    metadata:
                      name: your-service
                    spec:
                      type: NodePort
                      selector:
                        app: your-app
                      ports:
                        - protocol: TCP
                          port: 80
                          targetPort: 8080
                          nodePort: 30007
                    """
                    sh """
                        echo "${deploymentYaml}" > deployment.yaml
                        echo "${serviceYaml}" > service.yaml
                        ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@KUBERNETES_IP "kubectl apply -f -" < deployment.yaml
                        ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@KUBERNETES_IP "kubectl apply -f -" < service.yaml
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Build and deployment successful!"
        }
        failure {
            echo "Build or deployment failed!"
        }
    }
}
