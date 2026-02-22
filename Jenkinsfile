pipeline {
    agent any

    environment {
        IMAGE_NAME = 'begroom-settlement'
        CONTAINER_NAME = 'begroom-settlement'
        COMPOSE_PATH = '/home/ec2-user/load-test'
    }

    stages {
        stage('Clone') {
            steps {
                git credentialsId : 'begroom-settlement',
                    branch: 'test/settlement',
                    url: 'https://github.com/GroomBBang/BeGroom-Back.git'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    cd ${COMPOSE_PATH}
                    docker-compose stop ${CONTAINER_NAME}
                    docker-compose rm -f ${CONTAINER_NAME}
                    docker-compose up -d ${CONTAINER_NAME}
                """
            }
        }
    }

    post {
        success {
            echo '배포 성공!'
        }
        failure {
            echo '배포 실패!'
        }
    }
}