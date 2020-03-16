// import com.cicd.jenkins.CicdConfig as LibCicdConfig

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
  def (cicdCustom, cicdCustomProps) = customConfig('custom', 'CustomConfig')
  println cicdCustom
  
  // Getting application config
  node ('master') {
    stage('Initialize CICD') {
      sh 'echo "master - Stage: Initialize CICD"'
      checkout scm
      def cicdApp = readYaml file: 'config/AppConfig.yaml'
      println cicdApp
      if (cicd.build.job == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
    }
  }

  return cicd
}