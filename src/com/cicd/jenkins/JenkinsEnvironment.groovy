
package com.cicd.jenkins

// ----------------------------------------------------
// Environment Preperation Logic
// * facilitates the trigger by tag methodology
//
// Required Environment:
// * WORKSPACE: The workspace of te Jenkins build (usually provided by Jenkins)
// ----------------------------------------------------

class JenkinsEnvironment {
  // --- Resources
  def workspace
  def workspaceLib
  def gitCommit
  def tagName
  def changeId
  // def envGeneric

  // --- Data
  def prepTags

  // --- Constructor
  JenkinsEnvironment(context) {
    context.echo "Trigger by tag: git_commit ${context.env.GIT_COMMIT}"
    context.echo "Trigger by tag: tag_name ${context.env.TAG_NAME}"
    context.echo "Trigger by tag: change_id ${context.env.CHANGE_ID}"

    // Normalize when not present/null
    this.gitCommit = context.env.GIT_COMMIT ?: ''
    this.tagName = context.env.TAG_NAME ?: ''
    this.changeId = context.env.CHANGE_ID ?: ''

    context.echo "Trigger by tag: git_commit ${this.gitCommit}"
    context.echo "Trigger by tag: tag_name ${this.tagName}"
    context.echo "Trigger by tag: change_id ${this.changeId}"

    this.workspace = context.env.WORKSPACE
    this.workspaceLib = "${this.workspace}/../workspace@libs/cicd"
    this.prepTags = "${this.workspaceLib}/resources/com/cicd/jenkins/prepEnv.sh -git_commit ${this.gitCommit} -tag_name ${this.tagName} -change_id ${this.changeId}"
    this.prepTags.execute()
  }
}