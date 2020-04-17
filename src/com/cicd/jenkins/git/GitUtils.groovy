/*
 * GIT utilities
 * Jerome Mac Lean - CrossLogic Consulting <jerome@crosslogic-consulting.com>
 */

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

public String getApiUrl(Object scm) {
  String gitUrl = scm.getUserRemoteConfigs()[0].url
  Matcher matcher = (gitUrl =~ /.*:\/\/([^\/]+)\/[^\/]+\/[^\/]+.git/)
  /*  get the value matching the group */
  if (matcher[0][1] ==~ /.*github.com$/) {
    return 'api.github.com'
  } else {
    return matcher[0][1] + '/api/v3'
  }
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

public getGithubByTag(String tagName, Object scm) {
  /*  fetch the commit info*/
  String accountName = getCurrentAccountName(scm)
  String repoName = getCurrentRepoName(scm)
  String  apiUrl = getApiUrl(scm)
  def getResponseTagCommit
  def getResponseTag
  GString requestedUrl

  requestedUrl = "https://${apiUrl}/repos/${accountName}/${repoName}/git/refs/tags/${tagName}"
  try {
    getResponseTagCommit = httpRequest(acceptType: 'APPLICATION_JSON',
                                  authentication: 'github.main.cicd.api.credentials',
                                  url: requestedUrl)
  } catch (IllegalStateException e) {
    echo"Tag in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
    return null
  }
  def tagCommit = new JsonSlurper().parseText(getResponseTagCommit.content).object.sha

  requestedUrl = "https://${apiUrl}/repos/${accountName}/${repoName}/git/tags/${tagCommit}"
  try {
    getResponseTag = httpRequest(acceptType: 'APPLICATION_JSON',
                                  authentication: 'github.main.cicd.api.credentials',
                                  url: requestedUrl)
  } catch (IllegalStateException e) {
    echo"Tag in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
    return null
  }

  def tagInfoJson = new JsonSlurper().parseText(getResponseTag.content)
  def tagInfo = [:]
  tagInfo.tagCommit = tagInfoJson.sha ?: 'not set'
  tagInfo.gitCommit = tagInfoJson.object.sha ?: 'not set'
  tagInfo.tagName = tagInfoJson.tagger.name ?: 'not set'
  tagInfo.tagDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", tagInfoJson.tagger.date) ?: 'not set'

  return tagInfo
}

public getGithubRepoInfo(String gitCommit, Object scm) {
  /*  fetch the commit info*/
  String accountName = getCurrentAccountName(scm)
  String repoName = getCurrentRepoName(scm)
  String  apiUrl = getApiUrl(scm)
  def getResponseUser
  def getResponseRepo
  def getResponseCommit
  GString requestedUrl

  requestedUrl = "https://${apiUrl}/users/${accountName}"
  try {
    getResponseUser = httpRequest(acceptType: 'APPLICATION_JSON',
                                  authentication: 'github.main.cicd.api.credentials',
                                  url: requestedUrl)
  } catch (IllegalStateException e) {
    echo"User in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
    return null
  }

  requestedUrl = "https://${apiUrl}/repos/${accountName}/${repoName}"
  try {
    getResponseRepo = httpRequest(acceptType: 'APPLICATION_JSON',
                                  authentication: 'github.main.cicd.api.credentials',
                                  url: requestedUrl)
  } catch (IllegalStateException e) {
    echo"Repo in GitHub could not be found at URL: ${requestedUrl}. Error: ${e.message}"
    return null
  }

  requestedUrl = "https://${apiUrl}/repos/${accountName}/${repoName}/commits/${gitCommit}"
  try {
    getResponseCommit = httpRequest(acceptType: 'APPLICATION_JSON',
                                  authentication: 'github.main.cicd.api.credentials',
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

  def repoInfo = [:]
  repoInfo.repoName = repoInfoJson.name ?: 'not set'
  repoInfo.repoFullName = repoInfoJson.full_name ?: 'not set'
  repoInfo.repoDescription = repoInfoJson.description ?: 'not set'
  repoInfo.repoIsPrivate = repoInfoJson["private"] ?: 'not set'
  repoInfo.owner = repoInfoJson.owner.login ?: 'not set'
  repoInfo.ownerName = userInfoJson.name ?: 'not set'
  repoInfo.ownerCompany = userInfoJson.company ?: 'not set'
  if (repoInfoJson.owner) {
    repoInfo.ownerUrl = repoInfoJson.owner.html_url ?: 'not set'
    repoInfo.ownerAvatar = repoInfoJson.owner.avatar_url ?: 'not set'
  }
  if (commitInfoJson.commit.author) {
    repoInfo.authorName = commitInfoJson.commit.author.name ?: 'not set'
    repoInfo.authorEmail = commitInfoJson.commit.author.email ?: 'not set'
    repoInfo.authorDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", commitInfoJson.commit.author.date) ?: 'not set'
  }
  if (commitInfoJson.author) {
    repoInfo.authorName = commitInfoJson.author.login ?: 'not set'
    repoInfo.authorUrl = commitInfoJson.author.html_url ?: 'not set'
    repoInfo.authorAvatar = commitInfoJson.author.avatar_url ?: 'not set'
    repoInfo.authorDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", commitInfoJson.commit.author.date) ?: 'not set'
  }
  if (commitInfoJson.commit.committer) {
    repoInfo.committerName = commitInfoJson.commit.committer.name ?: 'not set'
    repoInfo.committerEmail = commitInfoJson.commit.committer.email ?: 'not set'
    repoInfo.committerDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", commitInfoJson.commit.committer.date) ?: 'not set'
  }
  if (commitInfoJson.committer) {
    repoInfo.committerName = commitInfoJson.committer.login ?: 'not set'
    repoInfo.committerUrl = commitInfoJson.committer.html_url ?: 'not set'
    repoInfo.committerAvatar = commitInfoJson.committer.avatar_url ?: 'not set'
    repoInfo.committerDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", commitInfoJson.commit.committer.date) ?: 'not set'
  } 

  return repoInfo
}

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
