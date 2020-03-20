import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger
import com.cicd.jenkins.utils.maps.MapMerge
import com.cicd.jenkins.utils.maps.MapUtils
import com.cicd.jenkins.git.GitInfoByTag
import static groovy.json.JsonOutput.*

def call() {
  def cicd = [:]
  def log
  def mapUtils = new MapUtils()

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
        // Note: if possible, could be moved out of "node" when workDirectory would be known beforehand
        cicdApp = readYaml file: 'config/AppConfig.yaml'
        cicd = mapUtils.merge(cicdGlobal, cicdApp)

        // Initialize logger
        // Pass it to env/'this' to be able to enable global debug (both in classes and containers)
        // MIND: env.<Integer>.getClass() will ALAWAYS by a String!!
        env.CICD_LOGLEVEL = cicd.loglevel
        Logger.init(this, [ logLevel: LogLevel[env.CICD_LOGLEVEL] ])
        log = new Logger(this)

        // Enhance cicd config (object) with git info, incl "trigger by tag" info
        // Note: if possible, could be moved out of "node" when TAG_NAME/CHANGE_ID would be known beforehand
        def gitInfoByTag = new GitInfoByTag(this)
        cicd = gitInfoByTag.info(cicd, scm)

        // Job management
        if (env.BUILD_NUMBER.toInteger() > cicd.job.throttle) {
          cicd.job.enabled = 0           // Disable staged

          // def Map orgAgent = MapUtils.deepCopy(cicd.job.agent)
          cicd.job.environment.agent.k8.name = 'base'   // Consume as minimal resources as possible.
        }

        // // Copy of used agent
        // def Map orgAgent = MapUtils.deepCopy(cicd.job.agent)

        log.debug("Library: CICD Configuration\n" + prettyPrint(toJson(cicd)))
        log.debug("Library: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true))

        // Kubernetes agent definition
        cicd.job.environment.agent.k8.config = k8sAgent(cicd.job.environment.agent.k8.name)
      }
  }
  return [cicd, log]
}
