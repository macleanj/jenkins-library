package com.cicd.jenkins.git

/**
 * There is currently no way to define these constants directly in the GitUtils script, so putting them in a separate class.
 */
final class GitUtilsConstants {
  public static final String TAG_BRANCHES_PREFIX = "tags/"
  public static final String DEPLOY_ON_PUSH_BRANCHES_PREFIX = "deploy-on-push/"
  public static final String GIT_VERSION_REGEX = "[0-9]+.[0-9]+.[0-9]+"
}
