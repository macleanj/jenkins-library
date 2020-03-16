/**
Default configuration definitions
Defaults:
- globalVars.buildThrottle = 1

*/

def call() {
  def cicd = [build: [:], git: [:], jenkins: [:], config: [:], env: [:]]
  cicd.build.debug = 0
  cicd.build.throttle = 1
  cicd.build.number = currentBuild.getNumber()

  // cicd.config.appName = "hello-world"
  // cicd.jenkins.agentLabel = cicd.config.appName.toLowerCase().replaceAll("[_]", "-")

pipeline {
    agent { label 'master' }
    stages {

        stage('Build0') {
            steps {
               sh 'echo "hello world0" '
            }  
        }
    }
}
  return cicd
}