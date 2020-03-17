import com.cicd.jenkins.MapMerge
import com.cicd.jenkins.GitInfo

def call() {
  def cicd = [:]
  def buildNumber = currentBuild.getNumber()
  def mapMerge = new MapMerge()

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
      echo "master - Stage: Initialize CICD"
      checkout scm

      // Merge config files
      cicdApp = readYaml file: 'config/AppConfig.yaml'
      cicd = mapMerge.merge(cicdCustom, cicdApp)

      // Get git info, incl "trigger by tag" info
      def gitInfo = new GitInfo(this)
      cicd.git = gitInfo.get('byTag')

      if (cicd.job.debug == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
    }
  }

  return cicd
}