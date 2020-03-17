import groovy.util.logging.Log4j
import org.apache.log4j.Level
import static groovy.json.JsonOutput.*
import com.cicd.jenkins.MapMerge
import com.cicd.jenkins.GitInfo

@Log4j
def call() {
  log.setLevel(Level.INFO)
  def cicd = [:]
  def mapMerge = new MapMerge()
    
  // Getting custom library config
  // Global config for the environment
  def (cicdGlobal, cicdGlobalProps) = globalConfig('config', 'GlobalConfig')

  // Getting application specific config
  // App config from built repo
  def cicdApp
  node ('master') {
    stage('Initialize CICD') {
      echo "master - Stage: Initialize CICD"
      checkout scm

      // Merge config files
      cicdApp = readYaml file: 'config/AppConfig.yaml'
      cicd = mapMerge.merge(cicdGlobal, cicdApp)
      debug = cicd.job.debug // Pass it to 'this' (context) in all methods to be able to enable global debug
      echo "DEBUG: " + debug
      log.info "INFO--------------------------"
      log.debug "DEBUG--------------------------"


      // Get git info, incl "trigger by tag" info
      // def gitInfo = new GitInfo(this)
      // cicd.git = gitInfo.get('byTag')

      if (debug == 1) {
        log.info "DEBUG: CICD Configuration\n" + prettyPrint(toJson(cicd))
        log.info "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true)
      }
    }
  }

  return cicd
}