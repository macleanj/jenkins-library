
package com.cicd.jenkins

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
  JenkinsEnvironment(context) {
    this.workspace = context.env.WORKSPACE
    this.workspace_lib = "${this.workspace}/../workspace@libs/cicd"
    // this.envGeneric = "${this.workspace_lib}/resources/com/cicd/jenkins/env.files/generic.groovy"
    this.prepTags = "${this.workspace_lib}/resources/com/cicd/jenkins/prepEnv.sh -git_commit 1a2b3c4d123456789 -tag_name bv-1.00 -change_id changeid123"


    // context.echo "Environment: ${this.workspace}"
    this.prepTags.execute()

    // load "${this.workspace_lib}/resources/com/cicd/jenkins/env.files/generic.groovy"
    // load ( "${this.workspace}/../workspace@libs/cicd-daimler-wltp-sim/resources/com/cicd/jenkins/env.files/tag_env.groovy"toString() )
    }
  }