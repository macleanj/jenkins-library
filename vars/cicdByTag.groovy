/*
 * Entrypoint for pipelines using the TriggerByTag workflow
 * Jerome Mac Lean - CrossLogic Consulting <jerome@crosslogic-consulting.com>
 */
import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger
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
  node ('master') {
    stage('cicdByTag (Library)') {
      echo "master - Stage: Initialize CICD"
      checkout scm

      // Merge Global and App config files. At this stage the merged config is only used for specific configurations like:
      // - build environment
      // - pr environments
      // - cicd.loglevel
      // Note: if possible, could be moved out of "node" when workDirectory would be known beforehand
      def cicdApp = readYaml file: 'config/AppConfig.yaml'
      cicd = mapUtils.merge(cicdGlobal, cicdApp)
      // In a later phase the application config.environments will be merged as last stage to be the most leading config.
      cicd.config.environments.app = cicdApp

      // Initialize logger
      // Pass it to env/'this' to be able to enable global debug (both in classes and containers)
      // MIND: env.<Integer>.getClass() will ALAWAYS by a String!!
      env.CICD_LOGLEVEL = cicd.loglevel
      Logger.init(this, [ logLevel: LogLevel[env.CICD_LOGLEVEL] ])
      log = new Logger(this)

      // Enhance cicd config (object) with git info, incl "trigger by tag" info
      // Note: if possible, could be moved out of "node" when TAG_NAME/CHANGE_ID would be known beforehand
      this.cicd = cicd
      def gitInfoByTag = new GitInfoByTag(this)
      cicd = gitInfoByTag.info(scm)
      this.cicd = cicd

      // Job management
      if (env.BUILD_NUMBER.toInteger() > cicd.job.throttle) {
        cicd.job.enabled = 0                          // Disable staged
        cicd.job.environment.agent.name = 'base'      // Consume as minimal resources as possible.
        log.warn("#####################################################################################")
        log.warn("#")
        log.warn("# Pipeline disabled by job throttle !!!")
        log.warn("#")
        log.warn("#####################################################################################")
      }

      // Kubernetes agent definition
      cicd.job.environment.agent.name = cicd.job.environment.container.builderAgentBase + "+" + cicd.job.environment.agent.name
      cicd.job.environment.agent = mapUtils.merge(cicd.job.environment.agent, k8sAgent(this))
      cicd.job.environment.agent.label = cicd.job.environment.agent.label + "-" + cicd.appName

      log.trace("Library: CICD Configuration\n" + prettyPrint(toJson(cicd)))
      log.trace("Library: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true))
    }
  }
  return [cicd, log]
}
