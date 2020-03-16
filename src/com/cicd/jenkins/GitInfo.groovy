package com.cicd.jenkins

class GitInfo implements Serializable {

  private final def script

  GitInfo(def script) {
    this.script = script
  }

  String commitMessage() {
    trimOutput("git log --format=%B -n 1 HEAD | head -n 1", 180)
  }

  String commitAuthor() {
    trimOutput("git log --format=\'%an\' -n 1 HEAD", 80)
  }

  String commitHash() {
    trimOutput("git rev-parse HEAD", 7)
  }

  private String trimOutput(String script, int maxLength) {
    String content = this.script.sh(script: script, returnStdout: true)
    content.substring(0, Math.min(maxLength, content.length())).trim()
  }

}
