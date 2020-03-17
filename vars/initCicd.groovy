def call() {
  def cicd = [:]
  def buildNumber = currentBuild.getNumber()
  
  // TEST ONLY: Getting example config
  // def (exampleCustom, exampleCustomProps) = cicdConfig('jenkins', 'CicdConfig')
  // println exampleCustom
  // println exampleCustom.deploy.dev.platformName

  // Getting custom library config
  // Global config for the environment
  def (cicdCustom, cicdCustomProps) = customConfig('custom', 'CustomConfig')

  // Map.metaClass.addNested = { Map rhs ->
  //   def lhs = delegate
  //   rhs.each { k, v -> lhs[k] = lhs[k] in Map ? lhs[k].addNested(v) : v }   
  //   lhs
  // }

  // Getting application specific config
  def cicdApp
  node ('master') {
    stage('Initialize CICD') {
      echo "master - Stage: Initialize CICD"
      checkout scm
      cicdApp = readYaml file: 'config/AppConfig.yaml'
      // Merge config files
      // cicd = cicdCustom.addNested( cicdApp )

      // TODO: change the below setting. This 
      if (cicdApp.job.debug == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
    }
  }

  println "cicdCustom : " + cicdCustom
  println "cicdApp    : " + cicdApp
  println "TAG_NAME   : " + TAG_NAME

  // println "cicd   : " + cicd




  return cicdApp
}