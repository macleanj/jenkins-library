#!/usr/bin/env bash
# Script to prepare environment for tags that control the entire environment.

now=$(date +%Y-%m-%d\ %H:%M:%S)
programName=$(basename $0)
programDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
baseName=$(echo ${programName} | sed -e 's/.sh//g')
debug=0

cd $programDir
source prepEnv.conf
source $baseName.conf

envFile="${CICD_FILES_DIR}/env.conf"

rm -rf ${CICD_FILES_DIR}
mkdir -p ${CICD_FILES_DIR}

buildTagType=$CICD_TAGS_BUILD_TAG
deployTagType=$CICD_TAGS_DEPLOY_TAG

Usage()
{
	echo "########################################################################################"
	echo "# "
	echo "# Script to prepare variables based on incoming information from Jenkins"
	echo "# Missing values are supported"
	echo "# "
	echo "# Usage: ${programName} -build_number <build number> -git_commit <git commit hash> -tag_name <supported tag> -change_id <changeid> [-d]"
	echo "# "
	echo "# Arguments:"
	echo "# build_number : Passes the build_number"
	echo "# git_commit   : Passes the git_commit"
  echo "# tag_name     : Passes the tag_name"
  echo "# change_id    : Passes the change_id"
  echo "# d|debug      : Enable debug mode"
  echo "# "
	echo "# Examples:"
	echo "# ${programName} -build_number 1 -git_commit 1a2b3c4d123456789 -tag_name bv-1.00 -change_id changeid123"
	echo "# "
	echo "# "
	echo "########################################################################################"
	exit 5
}

# Read and/or validate command-line parameters
ReadParams()
{
	[ $# -eq 0 ] && Usage

	while [ $# -gt 0 ]
	do
		case ${1}	in
    "-build_number")
			if [[ ${2} =~ ^- || -z ${2} ]]; then
        buildNumber="None"
  			shift 1
      else
        buildNumber="${2}"
			  shift 2
      fi
      ;;
    "-git_commit")
			if [[ ${2} =~ ^- || -z ${2} ]]; then
        gitHash="None"
  			shift 1
      else
        gitHash=$(echo "${2}" | cut -c1-8)
			  shift 2
      fi
      ;;
    "-tag_name")
			if [[ ${2} =~ ^- || -z ${2} ]]; then
        tagName="None"
  			shift 1
      else
        tagName="${2}"
			  shift 2
      fi
      ;;
    "-change_id")
			if [[ ${2} =~ ^- || -z ${2} ]]; then
        changeId="None"
  			shift 1
      else
        changeId="${2}"
			  shift 2
      fi
      ;;
		"-debug"|"-d")
      debug=1
      shift 1
			;;
		*)
			echo ">>>>> Wrong argument given: ${1}"; Usage
			;;
		esac
 	done

  # Obligated fields
  [[ -z $gitHash ]] && gitHash="None"
  [[ -z $tagName ]] && tagName="None"
  [[ -z $changeId ]] && changeId="None"
}

##########################
# Main script
##########################
ReadParams "$@"

declare -A mapTag2imageType
for mapping in $CICD_TAGS_TAG_MAPPING; do
  key=$(echo $mapping | awk -F "=" '{print $1}')
  value=$(echo $mapping | awk -F "=" '{print $2}')
  mapTag2imageType[$key]=$value
done

# Input determination
# tag_name never has a change_id and a change_id never has a tag_name
if [[ ! $tagName == "None" ]]; then
  # This is a tag
  triggerType="tag"
elif [[ ! $changeId == "None" ]]; then
  # This is a pullRequest
  triggerType="pullRequest"
else
  # unknown
  triggerType="unknown"
fi

# # Verify tagTypeKey tag character
# if [[ "$CICD_TAGS_NAME" == "" ]] && [[ "$CICD_TAGS_NAME" == "None" ]]; then
#   # Not a tag
#   buildEnabled=0
if [[ "$triggerType" == "tag" ]]; then
  # This is a tag
  [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
  [ $debug -eq 1 ] && echo -e "Triggered by tag"
  buildEnabled=1
  # Character extraction by expected format
  # - Build Single : $buildTagType<imageTypeKey>-<version>. Example: bh-1.01
  # - Build Multi  : $buildTagType<imageTypeKey>-<appName>-<version>. Example: bv-clojure-2.8.1
  # - Deploy       : $deployTagType<imageTypeKey>-<deployEnvironment>-<version>. Example: dv-prod-1.01
  tagTypeKey="${tagName:0:1}"
  imageTypeKey="${tagName:1:1}"
  partTwo=$(echo $tagName | awk -F "-" '{print $2}')
  partThree=$(echo $tagName | awk -F "-" '{print $3}')
  if [[ "$tagTypeKey" == "${buildTagType}" ]]; then
    tagType="build"
    if [ ! -z "${partThree}" ]; then
      appName=${partTwo}
      versionKey=${partThree}
    else
      versionKey=${partTwo}
    fi
  elif [[ "$tagTypeKey" == "${deployTagType}" ]]; then
    tagType="deployment"
    envKey=$(echo $tagName | awk -F "-" '{print $2}')
    versionKey=$(echo $tagName | awk -F "-" '{print $3}')
  fi
  [ $debug -eq 1 ] && echo "Receiving tag : $tagName"
  [ $debug -eq 1 ] && echo "tagTypeKey    : $tagTypeKey"
  [ $debug -eq 1 ] && echo "tagType       : $tagType"
  [ $debug -eq 1 ] && echo "imageTypeKey  : $imageTypeKey"
  [ $debug -eq 1 ] && echo "appName       : $appName"
  [ $debug -eq 1 ] && echo "envKey        : $envKey"
  [ $debug -eq 1 ] && echo "versionKey    : $versionKey"

  # tagTypeKey character
  [[ "$tagTypeKey" != "${buildTagType}" && "$tagTypeKey" != "${deployTagType}" ]] && buildEnabled=0
  [ $debug -eq 1 ] && echo -e "1: Enabled: $buildEnabled"

  # imageTypeKey character
  tmpKey=" $imageTypeKey "
  [[ ! " ${!mapTag2imageType[@]} " =~ \s*$tmpKey\s* ]] && buildEnabled=0
  [ $debug -eq 1 ] && echo -e "2: Enabled: $buildEnabled"

  # Trailing characters
  if [[ $buildEnabled == 1 ]]; then
    if [[ "$tagType" == "build" ]]; then
      # Build tag received
      CICD_TAGS_BUILD_IMAGE_TYPE=${mapTag2imageType[$imageTypeKey]}
      CICD_TAGS_BUILD_ENV="NA"
      CICD_TAGS_BUILD_VERSION=${versionKey}
      [[ ${mapTag2imageType[$imageTypeKey]} == "hash" ]] && CICD_TAGS_BUILD_VERSION=$gitHash

      # Build tag format
      [[ ! $tagName =~ ^[a-z]+-[0-9.]+$ ]] && buildEnabled=0
      [ $debug -eq 1 ] && echo -e "3: Enabled: $buildEnabled"
    elif [[ "$tagType" == "deployment" ]]; then
      # Deploy tag received
      CICD_TAGS_DEPLOY_IMAGE_TYPE=${mapTag2imageType[$imageTypeKey]}
      CICD_TAGS_DEPLOY_ENVIRONMENT=${envKey}
      CICD_TAGS_DEPLOY_VERSION=${versionKey}
      [[ ${mapTag2imageType[$imageTypeKey]} == "hash" ]] && CICD_TAGS_DEPLOY_VERSION=$gitHash

      # Deploy tag format
      [[ ! $tagName =~ ^[a-z]+-[a-z]+-[0-9.]+$ ]] && buildEnabled=0
      [ $debug -eq 1 ] && echo -e "3: Enabled: $buildEnabled"
      # envKey string
      tmpKey=" $envKey "
      [[ ! " ${CICD_TAGS_DEPLOY_ENV_LIST} " =~ $tmpKey ]] && buildEnabled=0
      [ $debug -eq 1 ] && echo -e "4: Enabled: $buildEnabled"
    fi
    # No else needed. Code is protected earlier
  fi
  [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
  [ $debug -eq 1 ] && echo "Tag enabled: $buildEnabled"
  [ $debug -eq 1 ] && echo "triggerType   : $triggerType"
  [ $debug -eq 1 ] && echo "tagType       : $tagType"
  [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
  [ $debug -eq 1 ] && echo "CICD_TAGS_BUILD_IMAGE_TYPE   : $CICD_TAGS_BUILD_IMAGE_TYPE"
  [ $debug -eq 1 ] && echo "CICD_TAGS_BUILD_ENVIRONMENT  : $CICD_TAGS_BUILD_ENV"
  [ $debug -eq 1 ] && echo "CICD_TAGS_BUILD_VERSION      : $CICD_TAGS_BUILD_VERSION"
  [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
  [ $debug -eq 1 ] && echo "CICD_TAGS_DEPLOY_IMAGE_TYPE  : $CICD_TAGS_DEPLOY_IMAGE_TYPE"
  [ $debug -eq 1 ] && echo "CICD_TAGS_DEPLOY_ENVIRONMENT : $CICD_TAGS_DEPLOY_ENVIRONMENT"
  [ $debug -eq 1 ] && echo "CICD_TAGS_DEPLOY_VERSION     : $CICD_TAGS_DEPLOY_VERSION"
  [ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
elif [ "$triggerType" == "pullRequest" ]; then
  # This is a pullRequest
  [ $debug -eq 1 ] && echo "Triggered by Pull Request"
  [ -f "${CICD_CONF_DIR_REPO}/${CICD_PR_CFG}.conf" ] && source <($programDir/confMerge.sh "${CICD_CONF_DIR_CENTRAL}/${CICD_PR_CFG}.conf" "${CICD_CONF_DIR_REPO}/${CICD_PR_CFG}.conf") || source "${CICD_CONF_DIR_CENTRAL}/${CICD_PR_CFG}.conf"
  CICD_TAGS_PR_IMAGE_TYPE="hash"
  CICD_PR_ID=$gitHash
  buildEnabled=$CICD_PR_BUILD_ENABLED
else
  # unknown
  [ $debug -eq 1 ] && echo "Triggered by unknown source. Disabled"
  buildEnabled=0
fi
if [ "$CICD_TAGS_JOBS_MULTI" == "0" ] && [ $buildNumber -gt 1 ]; then
  buildEnabled=0
fi
  
[ $debug -eq 1 ] && echo "Build enabled: $buildEnabled"

echo "CICD_BUILD_ENABLED=\"$buildEnabled\"" > ${envFile}
if [[ $buildEnabled == 1 ]]; then
  if [[ "$triggerType" == "tag" ]]; then
    if [[ "$tagType" == "build" ]]; then
      # Build tag received
      cat >> ${envFile} <<EOL
CICD_TAGS_TAG_TYPE="$tagType"
CICD_TAGS_IMAGE_TYPE="$CICD_TAGS_BUILD_IMAGE_TYPE"
CICD_TAGS_ID="$CICD_TAGS_BUILD_VERSION"
CICD_DEPLOY_ENABLED="0"
EOL
    elif [[ "$tagType" == "deployment" ]]; then
      # Deploy tag received
      cat >> ${envFile} <<EOL
CICD_TAGS_TAG_TYPE="$tagType"
CICD_TAGS_IMAGE_TYPE="$CICD_TAGS_DEPLOY_IMAGE_TYPE"
CICD_TAGS_DEPLOY_ENVIRONMENT="$CICD_TAGS_DEPLOY_ENVIRONMENT"
CICD_TAGS_ID="$CICD_TAGS_DEPLOY_VERSION"
CICD_DEPLOY_ENABLED="1"
EOL
    fi
  elif [[ "$triggerType" == "pullRequest" ]]; then
    # Pull Request received
    cat > ${envFile} <<EOL
CICD_BUILD_ENABLED="$CICD_PR_BUILD_ENABLED"
CICD_TAGS_TAG_TYPE="$triggerType"
CICD_TAGS_IMAGE_TYPE="$CICD_TAGS_PR_IMAGE_TYPE"
CICD_TAGS_DEPLOY_ENVIRONMENT="$CICD_PR_DEPLOY_ENVIRONMENT"
CICD_TAGS_ID="$CICD_PR_ID"
CICD_DEPLOY_ENABLED="$CICD_PR_DEPLOY_ENABLED"
EOL
  else
    cat >> ${envFile} <<EOL
CICD_TAGS_TAG_TYPE="Other"
CICD_TAGS_IMAGE_TYPE="none""
CICD_TAGS_DEPLOY_ENVIRONMENT="None"
CICD_TAGS_ID="None"
CICD_DEPLOY_ENABLED="0"
EOL
  fi
else
  cat >> ${envFile} <<EOL
CICD_DEPLOY_ENABLED="0"
EOL
fi
[ ! -z "${appName}" ] && echo "CICD_APP_NAME=\"$appName\"" >> ${envFile}

source $envFile
if [[ $CICD_DEPLOY_ENABLED == 1 ]]; then
  [ -f "${CICD_CONF_DIR_REPO}/generic.conf" ] && $programDir/confMerge.sh "${CICD_CONF_DIR_CENTRAL}/generic.conf" "${CICD_CONF_DIR_REPO}/generic.conf" >> $envFile || cat "${CICD_CONF_DIR_CENTRAL}/generic.conf" | egrep -v "^#|^[[:space:]]|^$" >> $envFile
  [ -f "${CICD_CONF_DIR_REPO}/deploy_${CICD_TAGS_DEPLOY_ENVIRONMENT}.conf" ] && $programDir/confMerge.sh "${CICD_CONF_DIR_CENTRAL}/deploy_${CICD_TAGS_DEPLOY_ENVIRONMENT}.conf" "${CICD_CONF_DIR_REPO}/deploy_${CICD_TAGS_DEPLOY_ENVIRONMENT}.conf" >> $envFile || cat "${CICD_CONF_DIR_REPO}/deploy_${CICD_TAGS_DEPLOY_ENVIRONMENT}.conf" | egrep -v "^#|^[[:space:]]|^$" >> $envFile
fi

[ $debug -eq 1 ] && echo "------------------------------------------------------------------------------------------"
[ $debug -eq 1 ] && echo "Final config:"
[ $debug -eq 1 ] && cat $envFile
