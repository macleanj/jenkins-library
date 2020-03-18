package com.cicd.jenkins.git

import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger
import com.cicd.jenkins.git.TriggerByTagConstants
import static groovy.json.JsonOutput.*

/*
 *
 * Tigger by tag examples, Character extraction by expected format:
 * - Build Single : $buildTag<imageTypeKey>-<version>. Example: bh-1.01
 * - Build Multi  : $buildTag<imageTypeKey>-<appName>-<version>. Example: bv-clojure-2.8.1
 * - Deploy       : $deployTag<imageTypeKey>-<deployEnvironment>-<version>. Example: dv-prod-1.01
 */

class GitInfo {
  // --- Resources
  def name
  def context
  def log
  
  // --- Constructor
  GitInfo(context) {
    this.context = context
    Logger.init(this.context, [ logLevel: LogLevel[context.env.CICD_LOGLEVEL] ])
    this.log = new Logger(this)
  }

  /**
  * Parse HEAD of current directory and return commit hash
  */
  def private getGitCommit() {
      git_commit = sh (
          script: 'git rev-parse HEAD',
          returnStdout: true
      ).trim()
      return git_commit
  }

  // --- Method Logic
  def get(def Map cicd, def String infoType) {
    def git = [:]
    git.branchName = context.env.BRANCH_NAME ?: ''
    git.changeId = context.env.CHANGE_ID ?: ''
    git.tagName = context.env.TAG_NAME ?: ''
    // git.gitHash = context.env.GIT_COMMIT ?: ''
    git.gitHash = this.getGitCommit()
    git.gitHashShort = git.gitHash ? git.gitHash.take(6) : ''
    
    if (git.changeId) {
      git.triggerType = "pullRequest"
    } else if (git.tagName) {
      git.triggerType = "tag"
    } else {
      git.triggerType = "unknown"
    }

    if (infoType == 'byTag') {
      log.trace("Extending to get extensive information based on git-tag")

      // START VALIDATION
      if (git.triggerType == 'tag') { 
        cicd.job.enabled = 0
        // Only supported patterns will match
        def TagNamePattern="^([${TriggerByTagConstants.buildTag}${TriggerByTagConstants.deployTag}])([${TriggerByTagConstants.versionTag}${TriggerByTagConstants.hashTag}])-([a-z0-9._]+)[-]*([0-9.]*)\$"
        def tagNameArray = (git.tagName =~ /${TagNamePattern}/)

        if (tagNameArray) {
          // Overall allowed tagPattern is matched
          println prettyPrint(toJson(tagNameArray[0]))
          def tagTypeKey = tagNameArray[0][1]
          def versionKey = tagNameArray[0][2]
          def partTwo = tagNameArray[0][3]
          def partThree = tagNameArray[0][4]

          // Build instantiation
          if (tagTypeKey == TriggerByTagConstants.buildTag) {
            git.tagType="build"
            // log.info("Tag:" + git.tagType)
            println "Tag: " + git.tagType
            if (partThree ==~ /[0-9.]+/) {
              // log.info("Tag: Build Multi")
              println "Tag: Build Multi"
              if (partTwo ==~ /[a-z_]+/) {
                cicd.job.enabled = 1
                cicd.job.buildEnabled = 1
                cicd.job.deployEnabled = 0
                git.appName = partTwo.toLowerCase().replaceAll("[_]", "-")

                // Set version
                git.versionId = (versionKey == TriggerByTagConstants.versionTag) ? partThree : git.gitHashShort

                // Used environment mapping
                cicd.job.environment = cicd.deploy[cicd.build.buildEnvironment]
              } else {
                // log.error("Tag: " + git.tagType + " tag not valid - bad appName pattern")
                println "Tag: " + git.tagType + " tag not valid - bad appName pattern"
              }
            } else if (partTwo ==~ /[0-9.]+/) {
              // TODO: add enableBuild after perfectly matching pattern
              // log.info("Tag: Build Single")
              println "Tag: Build Single"
              cicd.job.enabled = 1
              cicd.job.buildEnabled = 1
              cicd.job.deployEnabled = 0

              // Set version
              git.versionId = (versionKey == TriggerByTagConstants.versionTag) ? partTwo : git.gitHashShort

              // Used environment mapping
              cicd.job.environment = cicd.deploy[cicd.build.buildEnvironment]
            } else {
              // log.error("Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern")
              println "Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern"
            }

          // Deployment instantiation
          } else if (tagTypeKey == TriggerByTagConstants.deployTag) {
            git.tagType="deployment"
            // log.info("Tag:" + git.tagType)
            println "Tag:" + git.tagType
            if (partTwo ==~ /[a-z_]+/ && partThree ==~ /^[0-9.]+$/) {
              git.envKey = partTwo
              if (cicd.deploy.containsKey(git.envKey)) {
                cicd.job.enabled = 1
                cicd.job.buildEnabled = 1
                cicd.job.deployEnabled = 1

                // Set version
                git.versionId = (versionKey == TriggerByTagConstants.versionTag) ? partThree : git.gitHashShort

                // Used environment mapping
                cicd.job.environment = cicd.deploy[git.envKey]
              } else {
                // log.error("Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern")
                println "Tag: " + git.tagType + " tag not valid - not supported environment"
              }
            } else {
              // log.error("Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern")
              println "Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern"
            }
          
          // No "else" needed here as errors are caught in the initial pattern check

          }

        } else {
            git.tagType="overall"
            // log.error("Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern")
            println "Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern"
        }
      // Tag
      
      } else if (git.triggerType == "pullRequest") { 
        cicd.job.enabled = 1
        cicd.job.buildEnabled = cicd.pr.buildEnabled
        cicd.job.deployEnabled = cicd.pr.deployEnabled
        // log.info("pullRequest")
        println "pullRequest"

        // Set version
        git.versionId = git.gitHashShort

        // Used environment mapping
        cicd.job.environment = cicd.deploy[cicd.pr.buildEnvironment]
      } else { 
        // log.error("Unknown trigger")
        println "Unknown trigger"
      }
      // END VALIDATION

    }

    cicd.git = git
    return cicd
  }
}
//   tagTypeKey="${tagName:0:1}"
//   imageTypeKey="${tagName:1:1}"
//   partTwo=$(echo $tagName | awk -F "-" '{print $2}')
//   partThree=$(echo $tagName | awk -F "-" '{print $3}')
//   if [[ "$tagTypeKey" == "${buildTagType}" ]]; then
//     tagType="build"
//     if [ ! -z "${partThree}" ]; then
//       appName=$(echo ${partTwo} | sed -e 's/_/-/g')
//       versionKey=${partThree}
//     else
//       versionKey=${partTwo}
//     fi
//   elif [[ "$tagTypeKey" == "${deployTagType}" ]]; then
//     tagType="deployment"
//     envKey=$(echo $tagName | awk -F "-" '{print $2}')
//     versionKey=$(echo $tagName | awk -F "-" '{print $3}')
//   fi
//   [ $debug -eq 1 ] && echo "Receiving tag : $tagName"
//   [ $debug -eq 1 ] && echo "tagTypeKey    : $tagTypeKey"
//   [ $debug -eq 1 ] && echo "tagType       : $tagType"
//   [ $debug -eq 1 ] && echo "imageTypeKey  : $imageTypeKey"
//   [ $debug -eq 1 ] && echo "appName       : $appName"
//   [ $debug -eq 1 ] && echo "envKey        : $envKey"
//   [ $debug -eq 1 ] && echo "versionKey    : $versionKey"

//   # tagTypeKey character
//   [[ "$tagTypeKey" != "${buildTagType}" && "$tagTypeKey" != "${deployTagType}" ]] && buildEnabled=0
//   [ $debug -eq 1 ] && echo -e "1: Enabled: $buildEnabled"

//   # imageTypeKey character
//   tmpKey=" $imageTypeKey "
//   [[ ! " ${!mapTag2imageType[@]} " =~ \s*$tmpKey\s* ]] && buildEnabled=0
//   [ $debug -eq 1 ] && echo -e "2: Enabled: $buildEnabled"

//   # Trailing characters
//   if [[ $buildEnabled == 1 ]]; then
//     if [[ "$tagType" == "build" ]]; then
//       # Build tag received
//       CICD_TAGS_BUILD_IMAGE_TYPE=${mapTag2imageType[$imageTypeKey]}
//       CICD_TAGS_BUILD_ENV="NA"
//       CICD_TAGS_BUILD_VERSION=${versionKey}
//       [[ ${mapTag2imageType[$imageTypeKey]} == "hash" ]] && CICD_TAGS_BUILD_VERSION=$gitHash

//       # Build tag format
//       if [[ ! $tagName =~ ^[a-z]+-[0-9.]+$ ]] && [[ ! $tagName =~ ^[a-z]+-[\_a-z0-9.]+-[0-9.]+$ ]]; then buildEnabled=0; fi
//       [ $debug -eq 1 ] && echo -e "3: Enabled: $buildEnabled"
//     elif [[ "$tagType" == "deployment" ]]; then
//       # Deploy tag received
//       CICD_TAGS_DEPLOY_IMAGE_TYPE=${mapTag2imageType[$imageTypeKey]}
//       CICD_TAGS_DEPLOY_ENVIRONMENT=${envKey}
//       CICD_TAGS_DEPLOY_VERSION=${versionKey}
//       [[ ${mapTag2imageType[$imageTypeKey]} == "hash" ]] && CICD_TAGS_DEPLOY_VERSION=$gitHash

//       # Deploy tag format
//       [[ ! $tagName =~ ^[a-z]+-[a-z]+-[0-9.]+$ ]] && buildEnabled=0
//       [ $debug -eq 1 ] && echo -e "3: Enabled: $buildEnabled"
//       # envKey string
//       tmpKey=" $envKey "
//       [[ ! " ${CICD_TAGS_DEPLOY_ENV_LIST} " =~ $tmpKey ]] && buildEnabled=0
//       [ $debug -eq 1 ] && echo -e "4: Enabled: $buildEnabled"
//     fi
//     # No else needed. Code is protected earlier
//   fi
//   [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
//   [ $debug -eq 1 ] && echo "Tag enabled: $buildEnabled"
//   [ $debug -eq 1 ] && echo "triggerType   : $triggerType"
//   [ $debug -eq 1 ] && echo "tagType       : $tagType"
//   [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
//   [ $debug -eq 1 ] && echo "CICD_TAGS_BUILD_IMAGE_TYPE   : $CICD_TAGS_BUILD_IMAGE_TYPE"
//   [ $debug -eq 1 ] && echo "CICD_TAGS_BUILD_ENVIRONMENT  : $CICD_TAGS_BUILD_ENV"
//   [ $debug -eq 1 ] && echo "CICD_TAGS_BUILD_VERSION      : $CICD_TAGS_BUILD_VERSION"
//   [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
//   [ $debug -eq 1 ] && echo "CICD_TAGS_DEPLOY_IMAGE_TYPE  : $CICD_TAGS_DEPLOY_IMAGE_TYPE"
//   [ $debug -eq 1 ] && echo "CICD_TAGS_DEPLOY_ENVIRONMENT : $CICD_TAGS_DEPLOY_ENVIRONMENT"
//   [ $debug -eq 1 ] && echo "CICD_TAGS_DEPLOY_VERSION     : $CICD_TAGS_DEPLOY_VERSION"
//   [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
// elif [ "$triggerType" == "pullRequest" ]; then
//   # This is a pullRequest
//   [ $debug -eq 1 ] && echo "Triggered by Pull Request"
//   [ -f "${CICD_CONF_DIR_REPO}/${CICD_PR_CFG}.conf" ] && source <($programDir/confMerge.sh "${CICD_CONF_DIR_CENTRAL}/${CICD_PR_CFG}.conf" "${CICD_CONF_DIR_REPO}/${CICD_PR_CFG}.conf") || source "${CICD_CONF_DIR_CENTRAL}/${CICD_PR_CFG}.conf"
//   CICD_TAGS_PR_IMAGE_TYPE="hash"
//   CICD_PR_ID=$gitHash
//   buildEnabled=$CICD_PR_BUILD_ENABLED
// else
//   # unknown
//   [ $debug -eq 1 ] && echo "Triggered by unknown source. Disabled"
//   buildEnabled=0
// fi
// if [ "$CICD_TAGS_JOBS_MULTI" == "0" ] && [ $buildNumber -gt 1 ]; then
//   buildEnabled=0
// fi
  
// [ $debug -eq 1 ] && echo "Build enabled: $buildEnabled"

// echo "CICD_BUILD_ENABLED=\"$buildEnabled\"" > ${envFile}
// if [[ $buildEnabled == 1 ]]; then
//   if [[ "$triggerType" == "tag" ]]; then
//     if [[ "$tagType" == "build" ]]; then
//       # Build tag received
//       cat >> ${envFile} <<EOL
// CICD_TAGS_TAG_TYPE="$tagType"
// CICD_TAGS_IMAGE_TYPE="$CICD_TAGS_BUILD_IMAGE_TYPE"
// CICD_TAGS_ID="$CICD_TAGS_BUILD_VERSION"
// CICD_DEPLOY_ENABLED="0"
// EOL
//       [ ! -z "${appName}" ] && echo "CICD_TAGS_APP_NAME=\"$appName\"" >> ${envFile}
//     elif [[ "$tagType" == "deployment" ]]; then
//       # Deploy tag received
//       cat >> ${envFile} <<EOL
// CICD_TAGS_TAG_TYPE="$tagType"
// CICD_TAGS_IMAGE_TYPE="$CICD_TAGS_DEPLOY_IMAGE_TYPE"
// CICD_TAGS_DEPLOY_ENVIRONMENT="$CICD_TAGS_DEPLOY_ENVIRONMENT"
// CICD_TAGS_ID="$CICD_TAGS_DEPLOY_VERSION"
// CICD_DEPLOY_ENABLED="1"
// EOL
//     fi
//   elif [[ "$triggerType" == "pullRequest" ]]; then
//     # Pull Request received
//     cat > ${envFile} <<EOL
// CICD_BUILD_ENABLED="$CICD_PR_BUILD_ENABLED"
// CICD_TAGS_TAG_TYPE="$triggerType"
// CICD_TAGS_IMAGE_TYPE="$CICD_TAGS_PR_IMAGE_TYPE"
// CICD_TAGS_DEPLOY_ENVIRONMENT="$CICD_PR_DEPLOY_ENVIRONMENT"
// CICD_TAGS_ID="$CICD_PR_ID"
// CICD_DEPLOY_ENABLED="$CICD_PR_DEPLOY_ENABLED"
// EOL
//   else
//     cat >> ${envFile} <<EOL
// CICD_TAGS_TAG_TYPE="Other"
// CICD_TAGS_IMAGE_TYPE="none""
// CICD_TAGS_DEPLOY_ENVIRONMENT="None"
// CICD_TAGS_ID="None"
// CICD_DEPLOY_ENABLED="0"
// EOL
//   fi
// else
//   cat >> ${envFile} <<EOL
// CICD_DEPLOY_ENABLED="0"
// EOL
// fi

// source $envFile
// if [[ $CICD_BUILD_ENABLED == 1 ]]; then
//   [ -f "${CICD_CONF_DIR_REPO}/generic.conf" ] && $programDir/confMerge.sh "${CICD_CONF_DIR_CENTRAL}/generic.conf" "${CICD_CONF_DIR_REPO}/generic.conf" >> $envFile || cat "${CICD_CONF_DIR_CENTRAL}/generic.conf" | egrep -v "^#|^[[:space:]]|^$" >> $envFile
//   [ -f "${CICD_CONF_DIR_REPO}/${CICD_BUILD_CFG}.conf" ] && $programDir/confMerge.sh "${CICD_CONF_DIR_CENTRAL}/${CICD_BUILD_CFG}.conf" "${CICD_CONF_DIR_REPO}/${CICD_BUILD_CFG}.conf" >> $envFile || cat "${CICD_CONF_DIR_CENTRAL}/${CICD_BUILD_CFG}.conf" | egrep -v "^#|^[[:space:]]|^$" >> $envFile
// fi
// if [[ $CICD_DEPLOY_ENABLED == 1 ]]; then
//   [ -f "${CICD_CONF_DIR_REPO}/deploy_${CICD_TAGS_DEPLOY_ENVIRONMENT}.conf" ] && $programDir/confMerge.sh "${CICD_CONF_DIR_CENTRAL}/deploy_${CICD_TAGS_DEPLOY_ENVIRONMENT}.conf" "${CICD_CONF_DIR_REPO}/deploy_${CICD_TAGS_DEPLOY_ENVIRONMENT}.conf" >> $envFile || cat "${CICD_CONF_DIR_CENTRAL}/deploy_${CICD_TAGS_DEPLOY_ENVIRONMENT}.conf" | egrep -v "^#|^[[:space:]]|^$" >> $envFile
// fi
// sort -o $envFile $envFile

// [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
// [ $debug -eq 1 ] && echo "Final config:"
// [ $debug -eq 1 ] && cat $envFile

  // Specific mapping from tag to build and deployment information
  // Based on the workflow "Trigger by tag"
  // def byTag(Map[] sources) {
  // }


