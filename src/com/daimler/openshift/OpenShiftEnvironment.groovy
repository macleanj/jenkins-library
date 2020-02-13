
package com.daimler.openshift

import java.io.File 
import groovy.util.Eval

// ----------------------------------------------------
// Environment Preperation Logic
// * relies on the trigger by tag methodology
//
// Required Environment:
// * WORKSPACE: The workspace of te Jenkins build (usually provided by Jenkins)
// ----------------------------------------------------

class OpenShiftEnvironment {
  // --- Resources
  def workspace
  def workspace_lib
  // def envGeneric

  // --- Data
  def prepTags

  // --- Constructor
  OpenShiftEnvironment(context) {
    this.workspace = context.env.WORKSPACE
    this.workspace_lib = "${this.workspace}/../workspace@libs/cicd-daimler-wltp-sim"
    // this.envGeneric = "${this.workspace_lib}/resources/com/cicd/jenkins/env.files/generic.groovy"
    this.prepTags = "${this.workspace_lib}/resources/com/cicd/jenkins/_confConvert.sh bv-1.00 1a2b3c4d"


    // context.echo "Environment: ${this.workspace}"
    this.prepTags.execute()

env.APP_NAME="app_name"
env.CICD_BUILD_PATH="./build"
env.CICD_BUILD_FILE="Dockerfile"
env.CICD_GIT_REPO="https://github.com"
env.CICD_REGISTRY="jmaclean"
env.CICD_REGISTRY_URL=""
env.CICD_REGISTRY_CREDENTIALS="futurice-jmaclean-docker"
env.CICD_FILE_REPO="NA"
env.CICD_ARTIFACT_REPO="NA"
env.CICD_TAGS_BUILD_TAG="b"
env.CICD_TAGS_DEPLOY_TAG="d"
env.CICD_TAGS_TAG_MAPPING="v=version h=hash"
env.CICD_TAGS_DEPLOY_ENV_LIST="dev test stag prod"
env.CICD_TAGS_BUILD_ENV="build"
env.CICD_TAGS_PR_ENV="dev"

    // Source/load variable files
    new File("${this.workspace_lib}/resources/com/cicd/jenkins/env.files/generic.groovy").eachLine {  
      line -> "$line"
    } 
    
    // load "${this.workspace_lib}/resources/com/cicd/jenkins/env.files/generic.groovy"
    // load ( "${this.workspace}/../workspace@libs/cicd-daimler-wltp-sim/resources/com/cicd/jenkins/env.files/tag_env.groovy"toString() )
    }
  }