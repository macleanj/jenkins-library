package com.cicd.jenkins.git

import groovy.json.JsonSlurper
import java.util.regex.Matcher

public boolean isTag(String checkedOutBranchName) {
  boolean tagIsNotNull = checkedOutBranchName?.trim()
  return tagIsNotNull
}

public String getTagNameFromBranchName(String checkedOutBranchName) {
  String[] values = checkedOutBranchName.split('/')
  return values[values.length - 1]
}

public String getCurrentRepoName(Object scm) {
  String gitUrl = scm.getUserRemoteConfigs()[0].url
  Matcher matcher = (gitUrl =~ /.*[^\/]+\/[^\/]+\/([^\/]+).git/)
  /*  get the value matching the group */
  return matcher[0][1]
}

public String getCurrentAccountName(Object scm) {
  String gitUrl = scm.getUserRemoteConfigs()[0].url
  Matcher matcher = (gitUrl =~ /.*[^\/]+\/([^\/]+)\/[^\/]+.git/)
  /*  get the value matching the group */
  return matcher[0][1]
}

// public GithubRepoInfo getRepoInfo(Object scm) {
//   String currentRepoName = getCurrentRepoName(scm)
//   String currentAccountName = getCurrentAccountName(scm)
//   GithubCommitInfo gitCommitInfo = getGithubCommitInfo(currentAccountName + "/" + currentRepoName, gitCommit)
//   return gitCommitInfo
// }

public GithubRepoInfo getGithubRepoInfo(String gitCommit, Object scm) {
  /*  fetch the commit info*/
  String accountName = getCurrentAccountName(scm)
  String repoName = getCurrentRepoName(scm)
  def getResponseRepo
  def getResponseCommit
  GString requestedUrl

  requestedUrl = "https://api.github.com/users/${accountName}"
  try {
    getResponseUser = httpRequest(acceptType: 'APPLICATION_JSON',
                                  authentication: 'github.cicd.main.api.credentials',
                                  url: requestedUrl)
  } catch (IllegalStateException e) {
    echo"User in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
    return null
  }

  requestedUrl = "https://api.github.com/repos/${accountName}/${repoName}"
  try {
    getResponseRepo = httpRequest(acceptType: 'APPLICATION_JSON',
                                  authentication: 'github.cicd.main.api.credentials',
                                  url: requestedUrl)
  } catch (IllegalStateException e) {
    echo"Repo in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
    return null
  }

  requestedUrl = "https://api.github.com/repos/${accountName}/${repoName}/commits/${gitCommit}"
  try {
    getResponseCommit = httpRequest(acceptType: 'APPLICATION_JSON',
                                  authentication: 'github.cicd.main.api.credentials',
                                  url: requestedUrl)
  } catch (IllegalStateException e) {
    echo"Commit in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
    return null
  }

  // For releases
  // requestedUrl = "https://api.github.com/repos/${repoName}/releases/tags/${tagName}"
  
  def userInfoJson = new JsonSlurper().parseText(getResponseUser.content)
  def repoInfoJson = new JsonSlurper().parseText(getResponseRepo.content)
  def commitInfoJson = new JsonSlurper().parseText(getResponseCommit.content)

  GithubRepoInfo repoInfo = new GithubRepoInfo()
  repoInfo.repoName = repoInfoJson.name
  repoInfo.repoFullName = repoInfoJson.full_name
  repoInfo.repoDescription = repoInfoJson.description
  repoInfo.repoIsPrivate = repoInfoJson["private"]
  repoInfo.owner = repoInfoJson.owner.login
  repoInfo.ownerName = userInfoJson.name
  repoInfo.ownerCompany = userInfoJson.company
  repoInfo.ownerUrl = repoInfoJson.owner.html_url
  repoInfo.ownerAvatar = repoInfoJson.owner.avatar_url
  repoInfo.authorName = commitInfoJson.author.login
  repoInfo.authorUrl = commitInfoJson.author.html_url
  repoInfo.authorAvatar = commitInfoJson.author.avatar_url
  repoInfo.committerName = commitInfoJson.author.login
  repoInfo.committerUrl = commitInfoJson.author.html_url
  repoInfo.committerAvatar = commitInfoJson.author.avatar_url

  return repoInfo
}

// public GithubCommitInfo getCommitInfoForCurrentCommit(String gitCommit, Object scm) {
//   String currentRepoName = getCurrentRepoName(scm)
//   String currentAccountName = getCurrentAccountName(scm)
//   GithubCommitInfo gitCommitInfo = getGithubCommitInfo(currentAccountName + "/" + currentRepoName, gitCommit)
//   return gitCommitInfo
// }

// public GithubCommitInfo getGithubCommitInfo(String repoName, String gitCommit) {
//   /*  fetch the commit info*/
//   def commitResponse
//   GString requestedUrl = "https://api.github.com/repos/${repoName}/commits/${gitCommit}"
//   try {
//     commitResponse = httpRequest(acceptType: 'APPLICATION_JSON',
//                                   authentication: 'github.cicd.main.api.credentials',
//                                   url: requestedUrl)
//   } catch (IllegalStateException e) {
//     echo"Commit in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
//     return null
//   }

//   def commitInfoJson = new JsonSlurper().parseText(commitResponse.content)
//   GithubCommitInfo commitInfo = new GithubCommitInfo()
//   commitInfo.title = commitInfoJson.name
//   commitInfo.description = commitInfoJson.body
//   commitInfo.url = commitInfoJson.html_url
//   commitInfo.authorName = commitInfoJson.author.login
//   commitInfo.authorUrl = commitInfoJson.author.html_url
//   commitInfo.authorAvatar = commitInfoJson.author.avatar_url
//   commitInfo.committerName = commitInfoJson.author.login
//   commitInfo.committerUrl = commitInfoJson.author.html_url
//   commitInfo.committerAvatar = commitInfoJson.author.avatar_url
//   // commitInfo.isPreRelease = Boolean.valueOf(commitInfoJson.prerelease)
//   // commitInfo.tagName = tagName
//   return commitInfo
// }

// public GithubReleaseInfo getGithubReleaseInfo(String repoName, String tagName) {
//   /*  fetch the release info*/
//   def releaseResponse
//   GString requestedUrl = "https://api.github.com/repos/${repoName}/releases/tags/${tagName}"
//   try {
//     releaseResponse = httpRequest(acceptType: 'APPLICATION_JSON',
//                                   authentication: 'github.cicd.main.api.credentials',
//                                   url: requestedUrl)
//   } catch (IllegalStateException e) {
//     echo"Release in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
//     return null
//   }

//   def releaseInfoJson = new JsonSlurper().parseText(releaseResponse.content)
//   GithubReleaseInfo releaseInfo = new GithubReleaseInfo()
//   releaseInfo.title = releaseInfoJson.name
//   releaseInfo.description = releaseInfoJson.body
//   releaseInfo.url = releaseInfoJson.html_url
//   releaseInfo.authorName = releaseInfoJson.author.login
//   releaseInfo.authorUrl = releaseInfoJson.author.html_url
//   releaseInfo.authorAvatar = releaseInfoJson.author.avatar_url
//   releaseInfo.isPreRelease = Boolean.valueOf(releaseInfoJson.prerelease)
//   releaseInfo.tagName = tagName
//   return releaseInfo
// }

public String getMostRecentGitTag() {
  sh "git describe --abbrev=0 --tags >> git-version"
  String mostRecentGitTag = readFile 'git-version'

  /* remove any additional text from git version */
  String extractedGitVersion = (mostRecentGitTag =~ GitUtilsConstants.GIT_VERSION_REGEX)[0]
  echo "Retrieved most recent git tag: ${extractedGitVersion}"
  return extractedGitVersion
}

public String getLatestCommit() {
  return sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
}

public String getShortLatestCommit() {
  return getLatestCommit().take(6)
}
