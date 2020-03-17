import com.cicd.jenkins.MapMerge
import com.cicd.jenkins.GitInfo

def call() {
  def cicd = [:]
  def mapMerge = new MapMerge()

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
      def debug = cicd.job.debug // To pass it to 'this' (context) in all methods

      // Get git info, incl "trigger by tag" info
      def gitInfo = new GitInfo(this)
      cicd.git = gitInfo.get('byTag')

      if (debug == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
    }
  }

  return cicd
}