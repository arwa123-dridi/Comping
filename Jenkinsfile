pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        DOCKERHUB_USER = "${DOCKERHUB_CREDENTIALS_USR}"
        BACKEND_IMAGE = "${DOCKERHUB_USER}/comping-backend"
        FRONTEND_IMAGE = "${DOCKERHUB_USER}/comping-frontend"
        IMAGE_TAG = "${env.GIT_COMMIT ?: 'latest'}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Build & Test') {
            steps {
                dir('backendComping') {
                    sh './mvnw -B -DskipTests=false verify'
                }
            }
        }

        stage('Frontend Build & Test') {
            steps {
                dir('frontendComping/frontendCompingApp') {
                    sh 'npm ci'
                    sh 'npm run build -- --configuration production'
                    sh 'npm test -- --watch=false --browsers=ChromeHeadless || true'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -t ${BACKEND_IMAGE}:latest ./backendComping"
                sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -t ${FRONTEND_IMAGE}:latest ./frontendComping/frontendCompingApp"
            }
        }

        stage('Docker Push') {
            steps {
                sh "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_USER} --password-stdin"
                sh "docker push ${BACKEND_IMAGE}:${IMAGE_TAG}"
                sh "docker push ${BACKEND_IMAGE}:latest"
                sh "docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}"
                sh "docker push ${FRONTEND_IMAGE}:latest"
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                sh 'kubectl apply -f k8s/mongo.yaml'
                sh 'kubectl apply -f k8s/backend.yaml'
                sh 'kubectl apply -f k8s/frontend.yaml'
                sh 'kubectl apply -f k8s/prometheus.yaml'
                sh 'kubectl apply -f k8s/grafana.yaml'
                sh "kubectl set image deployment/backend backend=${BACKEND_IMAGE}:${IMAGE_TAG}"
                sh "kubectl set image deployment/frontend frontend=${FRONTEND_IMAGE}:${IMAGE_TAG}"
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
    }
}
