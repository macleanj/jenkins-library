package com.cicd.jenkins

class GitInfo {
  GitInfo(context) {
    this.context = context
  }

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
    return git
  }

  // Specific mapping from tag to build and deployment information
  // Based on the workflow "Trigger by tag"
  // def byTag(Map[] sources) {
  // }
}
