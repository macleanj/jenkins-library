def call(context, user, pass) {
  def cicd = context.cicd
  def log = context.log

  if (cicd.job.environment.container.buildingTool == 'img') {
    if (cicd.job.environment.registryType == 'docker') {
      log.info("[" + cicd.job.environment.container.buildingTool + "] jenkins-builder - Publish Image - Docker Image Registry")
      sh "img login -u $user -p $pass ${cicd.job.environment.registryUrl}"
      sh "img push ${cicd.job.environment.registryUrl}/${cicd.job.environment.registrySpace}/${cicd.appName}:${cicd.tag.versionId}"

    } else if (cicd.job.environment.registryType == 'imagestream') {
        sh "img tag ${cicd.job.environment.registryUrl}/${cicd.job.environment.registrySpace}/${cicd.appName}:${cicd.tag.versionId} ${cicd.job.environment.registryUrlLogin}/${cicd.job.environment.registrySpace}/${cicd.appName}:${cicd.tag.versionId}"
        sh "oc login https://${cicd.job.environment.registryUrlLogin} --token=$pass"
        sh "img login -u \$(oc whoami -t) -p \$(oc whoami -t) ${cicd.job.environment.registryUrlLogin}"
        sh "img push ${cicd.job.environment.registryUrlLogin}/${cicd.job.environment.registrySpace}/${cicd.appName}:${cicd.tag.versionId}"
    }
  }
}
