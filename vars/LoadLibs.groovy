// import com.ft.jenkins.BuildConfig
// import com.ft.jenkins.DeploymentUtils
// import com.ft.jenkins.git.GitUtils
// import com.ft.jenkins.git.GitUtilsConstants
// import com.ft.jenkins.git.GithubReleaseInfo

/**
 * Entry point that decides which pipeline to execute and how that pipeline is executed.
 */

def call(context) {
  
  // http://groovyandgrails.blogspot.com/2013/05/groovy-maps-nested.html
  def cicd = [:]
  cicd["env"] = [:]

  // GitUtils gitUtils = new GitUtils()
  // String currentBranch = (String) env.BRANCH_NAME
  // String currentTag = (String) env.TAG_NAME
  // DeploymentUtils deployUtils = new DeploymentUtils()

  // Determine trigger type
  cicd.env.triggerType = 'unknown'
  if (context.env.CHANGE_ID) {
    cicd.env.triggerType = 'pullRequest'
  } else if (context.env.TAG_NAME) {
    cicd.env.triggerType = 'tag'
  } else if (context.env.TAG_NAME) {
    // Reserved for Feature
    cicd.env.triggerType = 'feature'
  }

  context.echo "LoadLibs entry variables: ${cicd.env.triggerType}"

  // if (gitUtils.isTag(currentTag)) {
  //   String tagName = env.TAG_NAME
  //   GithubReleaseInfo releaseInfo = getReleaseInfoForCurrentTag(tagName)

  //   if (releaseInfo == null || releaseInfo.isPreRelease) {
  //     String envToDeploy = deployUtils.getTeamFromReleaseCandidateTag(tagName)
  //     teamEnvsBuildAndDeploy(context, envToDeploy, tagName, false)
  //   } else {
  //     upperEnvsBuildAndDeploy(releaseInfo, context)
  //   }
  // } else if (gitUtils.isDeployOnPushForBranch(currentBranch)) {
  //   if (currentBranch.contains(context.preprodEnvName) || currentBranch.contains(context.prodEnvName)) {
  //     echo "Skipping branch ${currentBranch} as ${GitUtilsConstants.DEPLOY_ON_PUSH_BRANCHES_PREFIX} can't be used to push to upper environments."
  //   } else {
  //     String releaseCandidateName = deployUtils.getReleaseCandidateName(currentBranch)
  //     teamEnvsBuildAndDeploy(context, deployUtils.getEnvironmentName(currentBranch), releaseCandidateName, true)
  //   }
  // } else {
  //   echo "Skipping branch ${currentBranch} as it is not a tag and it doesn't start with ${GitUtilsConstants.DEPLOY_ON_PUSH_BRANCHES_PREFIX}"
  // }

  return cicd
}

// public GithubReleaseInfo getReleaseInfoForCurrentTag(String tagName) {
//   GitUtils gitUtils = new GitUtils()
//   String currentRepoName = gitUtils.getCurrentRepoName(scm)

//   GithubReleaseInfo releaseInfo = gitUtils.getGithubReleaseInfo(tagName, currentRepoName)
//   return releaseInfo
// }
