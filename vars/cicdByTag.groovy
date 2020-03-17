import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger
import com.cicd.jenkins.utils.maps.MapMerge
import com.cicd.jenkins.git.GitInfo
import static groovy.json.JsonOutput.*

def call() {
  def cicd = [:]
  def log
  def mapMerge = new MapMerge()

  // Getting custom library config
  // Global config for the environment
  def (cicdGlobal, cicdGlobalProps) = globalConfig('config', 'GlobalConfig')

  // Getting application specific config
  // App config from built repo
  def cicdApp
  node ('master') {
    stage('Initialize CICD (Library)') {
      echo "master - Stage: Initialize CICD"
      checkout scm

      // Merge config files
      cicdApp = readYaml file: 'config/AppConfig.yaml'
      cicd = mapMerge.merge(cicdGlobal, cicdApp)

      // Initialize logger
      // Pass it to env/'this' to be able to enable global debug (both in classes and containers)
      // MIND: env.<Integer>.getClass() will ALAWAYS by a String!!
      env.CICD_LOGLEVEL = cicd.loglevel
      Logger.init(this, [ logLevel: LogLevel[env.CICD_LOGLEVEL] ])
      log = new Logger(this)

      log.error("Between-------------------")

      // Get git info, incl "trigger by tag" info
      def gitInfo = new GitInfo(this)
      cicd.git = gitInfo.get('byTag')

      log.error("CICD Configuration\n" + prettyPrint(toJson(cicd)))
      log.error("CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true))
    }
  }

  return [cicd, log]
}
