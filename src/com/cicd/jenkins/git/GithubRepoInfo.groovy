package com.cicd.jenkins.git

class GithubRepoInfo implements Serializable {
  String repoName
  String repoFullName
  String repoDescription
  Boolean repoIsPrivate
  String owner
  String ownerName
  String ownerCompany
  String ownerUrl
  String ownerAvatar
  String authorName
  String authorUrl
  String authorAvatar
  String committerName
  String committerUrl
  String committerAvatar
}
