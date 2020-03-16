@Library("custom@develop") _custom
// @Library("k8sagent@develop") _k8

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
  node ('master') {
    stage('Initialize CICD') {
      sh 'echo "master - Stage: Initialize CICD"'
      checkout scm
      def cicdApp = readYaml file: 'config/AppConfig.yaml'
      if (cicdApp.build.job == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
    }
  }

  println cicdCustom
  println cicdApp

  return cicd
}