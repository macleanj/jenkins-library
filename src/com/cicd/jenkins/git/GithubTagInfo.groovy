package com.cicd.jenkins.git

class GithubTagInfo implements Serializable {
  String tagCommit
  String gitCommit
  String tagName
  Date tagDate
}
