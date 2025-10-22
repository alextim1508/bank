pipeline {
  agent any

  stages {
    stage('Build JARs') {
      steps {
        script {
          if (isUnix()) {
            sh './gradlew clean bootJar'
          } else {
            bat 'gradlew.bat clean bootJar'
          }
        }
      }
    }

    stage('Build & Load Docker Images') {
      steps {
        script {
          def services = ['account', 'blocker', 'cash', 'exchange', 'exchange-generator', 'front-ui', 'notification', 'transfer']
          def versions = ['0.0.1-SNAPSHOT', '0.0.1-SNAPSHOT', '0.0.1-SNAPSHOT', '0.0.1-SNAPSHOT', '0.0.1-SNAPSHOT', '0.0.1-SNAPSHOT', '0.0.1-SNAPSHOT', '0.0.1-SNAPSHOT']

          echo "[INFO] Found ${services.size()} services to build and load."

          def parallelStages = [:]
          for (int i = 0; i < services.size(); i++) {
            def service = services[i]
            def version = versions[i]
            def imageName = "${service}-service"
            def stageName = "Build ${imageName}:${version}"

            echo "[INFO] Preparing stage: ${stageName} | ServiceDir: ${service} | Image: ${imageName} | Version: ${version}"

            parallelStages[stageName] = {
              buildAndLoadDockerImage(service, imageName, version)
            }
          }

          echo "[INFO] Starting parallel build and load stages..."
          parallel parallelStages
          echo "[INFO] All parallel stages completed."
        }
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              helm dependency update ./k8s
              helm upgrade --install mybank ./k8s
            '''
          } else {
            bat '''
              helm dependency update ./k8s
              helm upgrade --install mybank ./k8s
            '''
          }
        }
        echo "[INFO] Deployment completed."
      }
    }
  }

  post {
    always {
      echo "[INFO] Pipeline execution finished."
    }
    success {
      echo "[SUCCESS] Pipeline completed successfully!"
    }
    failure {
      echo "[ERROR] Pipeline failed! Check logs for details."
    }
  }
}

def buildAndLoadDockerImage(String service, String imageName, String version) {
  echo "[INFO] [${imageName}:${version}] Starting build from directory: ./${service}"

  if (isUnix()) {
    sh """
      docker build -t ${imageName}:${version} ./${service}
      minikube image load ${imageName}:${version}
    """
  } else {
    bat """
      docker build -t ${imageName}:${version} .\\${service}
      minikube image load ${imageName}:${version}
    """
  }

  echo "[INFO] [${imageName}:${version}] Build and load completed successfully."
}