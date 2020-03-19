package com.cicd.jenkins.git

class GithubCommitInfo implements Serializable {
  String authorName
  String authorUrl
  String authorAvatar
  String title
  String description
  String tagName
  String url
}
