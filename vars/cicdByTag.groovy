import static groovy.json.JsonOutput.*
import com.cicd.jenkins.MapMerge
import com.cicd.jenkins.GitInfo

def call() {
  def cicd = [:]
  def mapMerge = new MapMerge()
  Integer debug

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

      // Pass it to env/'this' to be able to enable global debug (both in classes and containers)
      // MIND: env.CICD_DEBUG.getClass() will LAWAYS by a String
      env.CICD_DEBUG = cicd.debug

      // Get git info, incl "trigger by tag" info
      def gitInfo = new GitInfo(this)
      cicd.git = gitInfo.get('byTag')

      if (cicd.debug == 1) {
        echo "DEBUG: CICD Configuration\n" + prettyPrint(toJson(cicd))
        echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true)
      }
    }
  }

  return cicd
}

// 6 TRACE   Designates finer-grained informational events than the DEBUG.
// 5 DEBUG 	Designates fine-grained informational events that are most useful to debug an application.
// 4 INFO 	Designates informational messages that highlight the progress of the application at coarse-grained level.
// 3 WARN 	Designates potentially harmful situations.
// 2 ERROR 	Designates error events that might still allow the application to continue running.
// 1 FATAL 	Designates very severe error events that will presumably lead the application to abort.
// 0 OFF 	The highest possible rank and is intended to turn off logging.
