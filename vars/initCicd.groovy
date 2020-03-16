def call() {
  def buildNumber = currentBuild.getNumber()
  
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

  println "cicdCustom : " + cicdCustom
  println "cicdApp    : " + cicdApp
  println "TAG_NAME   : " + TAG_NAME

  // Merge yamls from here

  return cicd
}