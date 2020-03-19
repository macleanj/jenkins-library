import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger
import com.cicd.jenkins.utils.maps.MapMerge
import com.cicd.jenkins.git.GitInfo
import com.cicd.jenkins.git.GitUtils
import com.cicd.jenkins.git.GitUtilsConstants
import com.cicd.jenkins.git.GithubReleaseInfo

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
        // // Checkout the repository and save the resulting metadata
        // def scmVars = checkout([
        //   $class: 'GitSCM'
        // ])
        // log.debug("scmVars: " + scmVars)
        checkout scm

        def x = sh(script: "git rev-parse HEAD", returnStdout: true)
        echo "GIT_COMMIT:  ${x}"

        GitUtils gitUtils = new GitUtils()
        GithubReleaseInfo releaseInfo = getReleaseInfoForCurrentTag(TAG_NAME)
        log.debug("releaseInfo\n" + prettyPrint(toJson(releaseInfo)))
        

        // Merge config files
        cicdApp = readYaml file: 'config/AppConfig.yaml'
        cicd = mapMerge.merge(cicdGlobal, cicdApp)

        // Initialize logger
        // Pass it to env/'this' to be able to enable global debug (both in classes and containers)
        // MIND: env.<Integer>.getClass() will ALAWAYS by a String!!
        env.CICD_LOGLEVEL = cicd.loglevel
        Logger.init(this, [ logLevel: LogLevel[env.CICD_LOGLEVEL] ])
        log = new Logger(this)

        // Enhance cicd config (object) with git info, incl "trigger by tag" info
        def gitInfo = new GitInfo(this)
        cicd = gitInfo.get(cicd, 'byTag')

        log.debug("CICD Configuration\n" + prettyPrint(toJson(cicd)))
        log.debug("CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true))
      }
  }


  return [cicd, log]
}


public GithubReleaseInfo getReleaseInfoForCurrentTag(String tagName) {
  GitUtils gitUtils = new GitUtils()
  String currentRepoName = gitUtils.getCurrentRepoName(scm)

  GithubReleaseInfo releaseInfo = gitUtils.getGithubReleaseInfo(tagName, currentRepoName)
  return releaseInfo
}
