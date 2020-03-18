package com.cicd.jenkins.git

import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger
import com.cicd.jenkins.git.TriggerByTagConstants

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
  def loglevel
  
  // --- Constructor
  GitInfo(context) {
    this.context = context
    Logger.init(this.context, [ logLevel: LogLevel[context.env.CICD_LOGLEVEL] ])
    this.log = new Logger(this)
  }

  // --- Method Logic
  def get(def String infoType) {
    def git = [:]
    git.tagName = context.env.TAG_NAME ?: ''
    git.changeId = context.env.CHANGE_ID ?: ''
    
    if (git.changeId) {
      git.triggerType = 'pullRequest'
    } else if (git.tagName) {
      git.triggerType = 'tag'
    } else {
      git.triggerType = 'unknown'
    }

    if (infoType == 'byTag') {
      log.trace("Extending to get extensive information based on git-tag")

      if (git.triggerType == 'tag') { 
        log.info("Tag:" + git.tagName)
        git.tagTypeKey = git.tagName.substring(0)
        git.imageTypeKey = git.tagName.substring(1)
        def partTwo = git.tagName =~/[a-z]+\-([^\-])+[\-]*[^\-]*/
        def partThree = git.tagName =~/[a-z]+\-[^\-]+[\-]*([^\-]*)/
        log.trace("PartTwo: " + partTwo + ", " + partTwo[0] + ", " + partTwo[0][0] + ", " + partTwo[0][1])
        log.trace("PartTwo: " + partThree + ", " + partThree[0] + ", " + partThree[0][0] + ", " + partThree[0][1])

  // public static final String buildTag = "b"
  // public static final String deployTag = "d"
  // public static final String versionTag = "v"
  // public static final String hashTag = "h"

        if (git.tagTypeKey == TriggerByTagConstants.buildTag) {
          git.tagType="build"
          log.info("Tag:" + git.tagType)
          if (partThree) {
            log.info("Tag: Build Multi")
            git.appName = partTwo.text.toLowerCase().replaceAll("[_]", "-")
            git.versionKey = partThree
          } else if (partTwo) {
            // TODO: add enableBuild after perfectly matching pattern
            log.info("Tag: Build Single")
            git.versionKey = partTwo
          } else {
            error.info("Tag: Build tag not valid")
            git.buildEnabled = 0
          }
        } else if (git.tagTypeKey == TriggerByTagConstants.deployTag) {
          git.tagType="deployment"
          log.info("Tag:" + git.tagType)
          // git.envKey = (git.tagName =~/[a-z]+-([^-])+[-]*[^-]*/)
          // git.versionKey = (git.tagName =~/[a-z]+-[^-]+[-]*([^-]*)/)
        }

        log.trace("------------------------------------------------------------------------------------------")
        log.trace("Triggered by tag")
      }
    }



    return git
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


