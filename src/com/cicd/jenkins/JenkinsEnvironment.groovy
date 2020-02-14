
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
    this.prepTags = "${this.workspace_lib}/resources/com/cicd/jenkins/prepEnv.sh -git_commit ${context.env.GIT_COMMIT} -tag_name ${context.env.TAG_NAME} -change_id ${context.env.CHANGE_ID}"
    this.prepTags.execute()
  }
}