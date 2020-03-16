/**
Default configuration definitions
Defaults:
- globalVars.buildThrottle = 1

*/

def call(Integer buildNumber) {
  def cicd = [build: [:], git: [:], jenkins: [:], config: [:], env: [:]]
  cicd.build.debug = 0
  cicd.build.throttle = 100
  cicd.build.number = currentBuild.getNumber()
  // cicd.config.appName = "hello-world"
  // cicd.jenkins.agentLabel = cicd.config.appName.toLowerCase().replaceAll("[_]", "-")

  return cicd
}