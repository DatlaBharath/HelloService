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
                sh 'mvn clean package -DskipTests=true'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def repoName = "helloservice"
                    def tag = "ratneshpuskar/${repoName.toLowerCase()}:${env.BUILD_NUMBER}"

                    sh "docker build -t ${tag} ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    script {
                        def repoName = "helloservice"
                        def tag = "ratneshpuskar/${repoName.toLowerCase()}:${env.BUILD_NUMBER}"

                        sh """
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                            docker push ${tag}
                            docker logout
                        """
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
                        spec:
                          replicas: 2
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
                          type: NodePort
                          selector:
                            app: helloservice
                          ports:
                          - port: 5000
                            targetPort: 5000
                            nodePort: 30007
                    """

                    sh """
                        ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@13.233.111.186 "kubectl apply -f -" <<< '${deploymentYaml}'
                    """
                    sh """
                        ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@13.233.111.186 "kubectl apply -f -" <<< '${serviceYaml}'
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo 'Deployment succeeded.'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}