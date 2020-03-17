package com.cicd.jenkins

class GitInfo {
  // --- Resources
  def context

  // --- Constructor
  GitInfo(context) {
    this.context = context
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
      context.echo "Extending to get extensive information based on git-tag"
      
    }

    return git
  }

  // Specific mapping from tag to build and deployment information
  // Based on the workflow "Trigger by tag"
  // def byTag(Map[] sources) {
  // }
}

