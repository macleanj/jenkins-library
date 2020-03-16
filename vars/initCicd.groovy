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


  // Working!!
  // def props = libraryResource('com/cicd/jenkins/CicdConfig.yaml')
  // def (mycicdConfig, cicdProps) = cicdConfig('jenkins', 'CicdConfig')
  // println mycicdConfig

  def cicdObject = readYaml file: 'com/cicd/jenkins/CicdConfig.yaml'
  println cicdObject


  // node ('master') {
  //   stage('Initialize CICD') {
  //     sh 'echo "master - Stage: Initialize CICD"'
  //     if (cicd.build.debug == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
  //   }
  // }

  return cicd
}