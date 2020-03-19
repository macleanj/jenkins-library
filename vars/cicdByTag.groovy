import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger
import com.cicd.jenkins.utils.maps.MapMerge
import com.cicd.jenkins.git.GitInfo
import com.cicd.jenkins.git.GitUtils
import com.cicd.jenkins.git.GithubRepoInfo

import static groovy.json.JsonOutput.*

def call() {
  def cicd = [:]
  def log
  def mapMerge = new MapMerge()

  println "scm url: " + scm.getUserRemoteConfigs()[0].url
  println "scm name: " + scm.getUserRemoteConfigs()[0].name
  println "scm refspec: " + scm.getUserRemoteConfigs()[0].refspec

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

        // Merge config files (TODO: can be moved out of "node" when workDirectory would be knwon)
        cicdApp = readYaml file: 'config/AppConfig.yaml'
        cicd = mapMerge.merge(cicdGlobal, cicdApp)

        // Initialize logger
        // Pass it to env/'this' to be able to enable global debug (both in classes and containers)
        // MIND: env.<Integer>.getClass() will ALAWAYS by a String!!
        env.CICD_LOGLEVEL = cicd.loglevel
        Logger.init(this, [ logLevel: LogLevel[env.CICD_LOGLEVEL] ])
        log = new Logger(this)

        // Get commit/tag (TODO: can be moved out of "node" when TAG_NAME and GIT_COMMIT would be knwon)
        def gitCommit = sh(script: "git rev-parse HEAD", returnStdout: true)
        echo "GIT_COMMIT:  ${gitCommit}"

        // GitUtils gitUtils = new GitUtils()
        // GithubCommitInfo gitCommitInfo = getCommitInfoForCurrentCommit(gitCommit)
        // echo "gitCommitInfo\n" + prettyPrint(toJson(gitCommitInfo))

        GitUtils gitUtils = new GitUtils()
        GithubRepoInfo gitCommitInfo = gitUtils.getGithubRepoInfo(gitCommit, scm)
        echo "gitCommitInfo\n" + prettyPrint(toJson(gitCommitInfo))

        // Enhance cicd config (object) with git info, incl "trigger by tag" info
        def gitInfo = new GitInfo(this)
        cicd = gitInfo.get(cicd, 'byTag')

        log.debug("CICD Configuration\n" + prettyPrint(toJson(cicd)))
        log.debug("CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true))
      }
  }


  return [cicd, log]
}


// public GithubCommitInfo getCommitInfoForCurrentCommit(String gitCommit) {
//   GitUtils gitUtils = new GitUtils()
//   String currentRepoName = gitUtils.getCurrentRepoName(scm)
//   String currentAccountName = gitUtils.getCurrentAccountName(scm)

//   GithubCommitInfo gitCommitInfo = gitUtils.getGithubCommitInfo(currentAccountName + "/" + currentRepoName, gitCommit)
//   return gitCommitInfo
// }
