def call() {
  // https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readyaml-read-yaml-from-files-in-the-workspace-or-text

  // Not working somehow
  // def cicdConfig = new LibCicdConfig()
  // def test = cicdConfig.get()
  // println test

  def cicd = [build: [:], git: [:], jenkins: [:], config: [:], env: [:]]
  cicd.build.debug = 1
  cicd.build.throttle = 1
  cicd.build.number = currentBuild.getNumber()


  // TEST ONLY: Getting example config
  // def (exampleCustom, exampleCustomProps) = cicdConfig('jenkins', 'CicdConfig')
  // println exampleCustom
  // println exampleCustom.deploy.dev.platformName

  // Getting custom library config
  // Global config for the environment
  def (cicdCustom, cicdCustomProps) = customConfig('custom', 'CustomConfig')
  
  // Getting application specific config
  def cicdApp
  node ('master') {
    stage('Initialize CICD') {
      sh 'echo "master - Stage: Initialize CICD"'
      checkout scm
      cicdApp = readYaml file: 'config/AppConfig.yaml'
      if (cicdApp.job.debug == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
    }
  }

  println cicdCustom
  println cicdApp

  return cicd
}