package com.cicd.jenkins.git

import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger
import com.cicd.jenkins.git.GitUtils
import com.cicd.jenkins.git.GitTags
import com.cicd.jenkins.utils.maps.MapUtils
import static groovy.json.JsonOutput.*

/*
 *
 * Tigger by tag examples, Character extraction by expected format:
 * - Build Single : $buildTag<imageTypeKey>-<version>. Example: bh-1.01
 * - Build Multi  : $buildTag<imageTypeKey>-<appName>-<version>. Example: bv-clojure-2.8.1
 * - Deploy       : $deployTag<imageTypeKey>-<deployEnvironment>-<version>. Example: dv-prod-1.01
 */

class GitInfoByTag {
  // --- Resources
  def context
  def cicd
  def log
  def gitUtils
  def mapUtils
  
  // --- Constructor
  GitInfoByTag(context) {
    this.context = context

    // Instantiate methods
    Logger.init(this.context, [ logLevel: LogLevel[context.env.CICD_LOGLEVEL] ])
    this.cicd = context.cicd
    this.log = new Logger(this)
    this.gitUtils = new GitUtils()
    this.mapUtils = new MapUtils()
  }

  // --- Method Logic
  def info(scm) {
    def triggerType
    def git = [:]
    def tag = [:]
    def changeId = context.env.CHANGE_ID ?: ''
    def tagName = context.env.TAG_NAME ?: ''
    
    if (changeId) {
      triggerType = "pullRequest"
    } else if (tagName) {
      triggerType = "tag"
      def gitTag = gitUtils.getGithubByTag(tagName, scm)
      def gitRepo = gitUtils.getGithubRepoInfo(gitTag.gitCommit, scm)
      git = gitTag + gitRepo
    } else {
      triggerType = "unknown"
    }
    git.changeId = changeId
    git.tagName = tagName
    git.triggerType = triggerType

    // START VALIDATION
    if (git.triggerType == 'tag') { 
      cicd.job.enabled = 0
      // Only supported patterns will match
      def TagNamePattern="^([${GitTags.buildTag}${GitTags.deployTag}])([${GitTags.versionTag}${GitTags.hashTag}])-([a-z0-9._]+)[-]*([0-9.]*)\$"
      def tagNameArray = (git.tagName =~ /${TagNamePattern}/)

      if (tagNameArray) {
        // Overall allowed tagPattern is matched
        def tagTypeKey = tagNameArray[0][1]
        def versionKey = tagNameArray[0][2]
        def partTwo = tagNameArray[0][3]
        def partThree = tagNameArray[0][4]

        // Build instantiation
        if (tagTypeKey == GitTags.buildTag) {
          tag.tagType="build"
          log.info("Tag:" + tag.tagType)
          if (partThree ==~ /[0-9.]+/) {
            log.info("Tag: Build Multi")
            if (partTwo ==~ /[a-z_0-9.]+/) {
              cicd.job.enabled = 1
              cicd.job.buildEnabled = 1
              cicd.job.deployEnabled = 0
              tag.appName = partTwo.toLowerCase().replaceAll("[_]", "-")

              // Tag appName is leading over cicd.appName
              cicd.appName = tag.appName

              // Set version
              tag.versionId = (versionKey == GitTags.versionTag) ? partThree : git.gitHashShort

              // Used environment mapping
              cicd.job.environment = cicd.config.environments[cicd.config.build.buildEnvironment]
            } else {
              log.error("Tag: " + tag.tagType + " tag not valid - bad appName pattern")
                          }
          } else if (partTwo ==~ /[0-9.]+/) {
            // TODO: add enableBuild after perfectly matching pattern
            log.info("Tag: Build Single")
                        cicd.job.enabled = 1
            cicd.job.buildEnabled = 1
            cicd.job.deployEnabled = 0

            // Set version
            tag.versionId = (versionKey == GitTags.versionTag) ? partTwo : git.gitHashShort

            // Used environment mapping
            cicd.job.environment = cicd.config.environments[cicd.config.build.buildEnvironment]
          } else {
            log.error("Tag: " + tag.tagType + " tag not valid - bad " + tag.tagType + " tag pattern")
                      }

        // Deployment instantiation
        } else if (tagTypeKey == GitTags.deployTag) {
          tag.tagType="deployment"
          log.info("Tag:" + tag.tagType)
          if (partTwo ==~ /[a-z_]+/ && partThree ==~ /^[0-9.]+$/) {
            git.envKey = partTwo
            if (cicd.config.environments.containsKey(git.envKey)) {
              cicd.job.enabled = 1
              cicd.job.buildEnabled = 1
              cicd.job.deployEnabled = 1

              // Set version
              tag.versionId = (versionKey == GitTags.versionTag) ? partThree : git.gitHashShort

              // Used environment mapping
              cicd.job.environment = cicd.config.environments[git.envKey]
            } else {
              log.error("Tag: " + tag.tagType + " tag not valid - bad " + tag.tagType + " tag pattern")
                          }
          } else {
            log.error("Tag: " + tag.tagType + " tag not valid - bad " + tag.tagType + " tag pattern")
                      }
        
        // No "else" needed here as errors are caught in the initial pattern check

        }

      } else {
        tag.tagType="overall"
        log.error("Tag: " + tag.tagType + " tag not valid - bad " + tag.tagType + " tag pattern")
      }
      // Tag
    
    } else if (git.triggerType == "pullRequest") { 
      cicd.job.enabled = 1
      cicd.job.buildEnabled = cicd.config.pr.buildEnabled
      cicd.job.deployEnabled = cicd.config.pr.deployEnabled
      log.info("pullRequest")
      
      // Set version
      tag.versionId = git.gitHashShort

      // Used environment mapping
      cicd.job.environment = cicd.config.environments[cicd.config.pr.buildEnvironment]
      // pullRequest

    } else { 
      log.error("Unknown trigger")
    }
    // END VALIDATION

    // Merge environment configurations. Leading from top to down
    // - AppSpecific
    // - AppGeneric
    // - Specific
    // - Generic
    def Map envGenericEnv = mapUtils.deepCopy(cicd.config.environments.generic)
    def Map envSpecificEnv = mapUtils.deepCopy(cicd.job.environment)
    def Map envAppGenericEnv = mapUtils.deepCopy(cicd.config.environments.app[cicd.appFamily].generic)
    def Map envAppSpecificEnv = mapUtils.deepCopy(cicd.config.environments.app[cicd.appFamily][cicd.job.environment.key])
    cicd.job.environment = mapUtils.merge(envGeneric, envSpecific)
    cicd.job.environment = mapUtils.merge(cicd.job.environment, envAppGenericEnv)
    cicd.job.environment = mapUtils.merge(cicd.job.environment, envAppSpecificEnv)

    cicd.git = git
    cicd.tag = tag
    return cicd
  }
}
