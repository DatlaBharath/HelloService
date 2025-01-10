pipeline {
    agent any

    tools {
        maven 'Maven'
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/DatlaBharath/HelloService'
            }
        }
        
        stage('Build Application') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    def repoName = 'helloservice'
                    def imageTag = "ratneshpuskar/${repoName.toLowerCase()}:${env.BUILD_NUMBER}"
                    
                    sh """
                       docker build -t ${imageTag} .
                    """
                }
            }
        }
        
        stage('Push Docker Image to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    script {
                        def repoName = 'helloservice'
                        def imageTag = "ratneshpuskar/${repoName.toLowerCase()}:${env.BUILD_NUMBER}"
                        
                        sh """
                           echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin
                           docker push ${imageTag}
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def repoName = 'helloservice'
                    def imageTag = "ratneshpuskar/${repoName.toLowerCase()}:${env.BUILD_NUMBER}"
                    
                    def deploymentYaml = """
                    apiVersion: apps/v1
                    kind: Deployment
                    metadata:
                      name: ${repoName}
                    spec:
                      replicas: 1
                      selector:
                        matchLabels:
                          app: ${repoName}
                      template:
                        metadata:
                          labels:
                            app: ${repoName}
                        spec:
                          containers:
                          - name: ${repoName}
                            image: ${imageTag}
                            ports:
                            - containerPort: 5000
                    """
                    
                    def serviceYaml = """
                    apiVersion: v1
                    kind: Service
                    metadata:
                      name: ${repoName}-service
                    spec:
                      type: NodePort
                      selector:
                        app: ${repoName}
                      ports:
                      - port: 5000
                        targetPort: 5000
                        nodePort: 30007
                    """
                    
                    writeFile(file: 'deployment.yaml', text: deploymentYaml)
                    writeFile(file: 'service.yaml', text: serviceYaml)
                    
                    sh """
                       ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.110.130.203 "kubectl apply -f -" < deployment.yaml 
                       ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.110.130.203 "kubectl apply -f -" < service.yaml
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo 'Job completed successfully!'
        }
        failure {
            echo 'Job failed!'
        }
    }
}