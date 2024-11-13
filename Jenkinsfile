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
                    def imageName = "ratneshpuskar/helloservice:${env.BUILD_NUMBER}"
                    sh """
                       docker build -t ${imageName} .
                    """
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUsername')]) {
                    script {
                        def imageName = "ratneshpuskar/helloservice:${env.BUILD_NUMBER}"
                        sh """
                           echo "${dockerHubPassword}" | docker login -u "${dockerHubUsername}" --password-stdin
                           docker push ${imageName}
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

                    sh """
                       echo """${deploymentYaml}""" > deployment.yaml
                       echo """${serviceYaml}""" > service.yaml
                       ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@13.232.152.53 "kubectl apply -f -" < deployment.yaml
                       ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@13.232.152.53 "kubectl apply -f -" < service.yaml
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Job succeeded!'
        }
        failure {
            echo 'Job failed!'
        }
    }
}