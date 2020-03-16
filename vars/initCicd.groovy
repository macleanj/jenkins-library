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

node ('master') {
  stage('Initialize CICD') {
    sh 'echo "master - Stage: Initialize CICD"'
    if (cicd.build.debug == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
  }
}

  return cicd
}