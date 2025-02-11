---
version: 2
plan:
    project-key: DEP
    name: Maven Build and Docker Deploy Plan
    key: BUILD

stages:
    - Build and Deploy:
        jobs:
        - Build and Package

Build and Package:
    tasks:
    - script:
        - mvn clean install -DskipTests
        - docker build -t sakthisiddu1/helloservice:3 .
        - echo "Sakthisid@1" | docker login -u "sakthisiddu1" --password-stdin
        - docker push sakthisiddu1/helloservice:3
                
    - script:
        - echo "Creating deployment.yaml"
        - |
            cat <<EOF > deployment.yaml
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
                    image: sakthisiddu1/helloservice:3
                    ports:
                    - containerPort: 5000
            EOF
                
        - echo "Creating service.yaml"
        - |
            cat <<EOF > service.yaml
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
            EOF
                
    - script:
        - echo "Deploying to Kubernetes"
        - scp -o StrictHostKeyChecking=no -i /var/test.pem deployment.yaml service.yaml ubuntu@65.2.143.137:/home/ubuntu/
        - ssh -o StrictHostKeyChecking=no -i /var/test.pem ubuntu@65.2.143.137 "kubectl apply -f /home/ubuntu/deployment.yaml"
        - ssh -o StrictHostKeyChecking=no -i /var/test.pem ubuntu@65.2.143.137 "kubectl apply -f /home/ubuntu/service.yaml"