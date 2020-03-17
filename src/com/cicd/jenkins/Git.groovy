package com.cicd.jenkins

class Git {
  def info() {
    def git = [:]
    (env.TAG_NAME) ? git.tagName = env.TAG_NAME
    (env.CHANGE_ID) ? git.changeId = env.CHANGE_ID
    
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

