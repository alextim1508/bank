pipeline {
    agent any

    stages {
        stage('Package services') {
            steps {
                sh "chmod +x -R ${env.WORKSPACE}"
                sh '''
                    gradlew.bat clean bootJar
                '''
            }
        }

        stage('Dockerize accounts') {
            steps {
                sh '''
                    docker build -t account-service:0.0.1-SNAPSHOT ./account
                '''
            }
        }

        stage('Push accounts image to minikube') {
            steps {
                sh '''
                    minikube image load account-service:0.0.1-SNAPSHOT
                '''
            }
        }

        stage('Dockerize blocker') {
            steps {
                sh '''
                   docker build -t blocker-service:0.0.1-SNAPSHOT ./blocker
                '''
            }
        }

        stage('Push blocker image to minikube') {
            steps {
                sh '''
                   minikube image load blocker-service:0.0.1-SNAPSHOT
                '''
            }
        }


        stage('Dockerize cash') {
            steps {
                sh '''
                    docker build -t cash-service:0.0.1-SNAPSHOT ./cash
                '''
            }
        }

        stage('Push cash image to minikube') {
            steps {
                sh '''
                    minikube image load cash-service:0.0.1-SNAPSHOT
                '''
            }
        }

        stage('Dockerize exchange') {
            steps {
                sh '''
                    docker build -t exchange-service:0.0.1-SNAPSHOT ./exchange
                '''
            }
        }

        stage('Push exchange image to minikube') {
            steps {
                sh '''
                    minikube image load exchange-service:0.0.1-SNAPSHOT
                '''
            }
        }

        stage('Dockerize exchange-generator') {
            steps {
                sh '''
                    docker build -t exchange-generator-service:0.0.1-SNAPSHOT ./exchange-generator
                '''
            }
        }

        stage('Push exchange-generator image to minikube') {
            steps {
                sh '''
                    minikube image load exchange-generator-service:0.0.1-SNAPSHOT
                '''
            }
        }

        stage('Dockerize front') {
            steps {
                sh '''
                    docker build -t front-ui-service:0.0.1-SNAPSHOT ./front-ui
                '''
            }
        }

        stage('Push front image to minikube') {
            steps {
                sh '''
                    minikube image load front-ui-service:0.0.1-SNAPSHOT
                '''
            }
        }

        stage('Dockerize notifications') {
            steps {
                sh '''
                    docker build -t notifications-service:0.0.1-SNAPSHOT ./notifications
                '''
            }
        }

        stage('Push notifications image to minikube') {
            steps {
                sh '''
                    minikube image load notification-service:0.0.1-SNAPSHOT
                '''
            }
        }

        stage('Dockerize transfer') {
            steps {
                sh '''
                    docker build -t transfer-service:0.0.1-SNAPSHOT ./transfer
                '''
            }
        }

        stage('Push transfer image to minikube') {
            steps {
                sh '''
                    minikube image load transfer-service:0.0.1-SNAPSHOT
                '''
            }
        }

        stage('Dockerize keycloak') {
            steps {
                sh '''
                    docker pull quay.io/keycloak/keycloak:26.1.3
                '''
            }
        }

        stage('Push keycloak image to minikube') {
            steps {
                sh '''
                    minikube image load quay.io/keycloak/keycloak:26.1.3
                '''
            }
        }

        stage('Установка Helm') {
            steps {
                sh '''
                    helm dependency update ./k8s
                    helm install mybank ./k8s
                '''
            }
        }
    }
}