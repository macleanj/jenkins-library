/*
 * Function to build images based on choosen build method
 * Jerome Mac Lean - CrossLogic Consulting <jerome@crosslogic-consulting.com>
 */
def call(context, user, pass) {
  def cicd = context.cicd
  def log = context.log
  def dir = "."

  if (cicd.tag.appName) {
    dir = "./" + cicd.tag.appName
  }

  def imageName = cicd.job.environment.registryUrl + "/" + cicd.job.environment.registrySpace + "/" + cicd.job.environment.appNamePrefix + cicd.appName + cicd.job.environment.appNamePostfix + ":" + cicd.tag.versionId
  if (cicd.job.environment.container.buildingTool == 'img') {
    if (cicd.job.environment.registryType == 'docker') {
      log.info("[" + cicd.job.environment.container.buildingTool + "] jenkins-builder - Build Image - Docker Image Registry")
      sh "img login -u $user -p $pass ${cicd.job.environment.registryUrl}"
      sh "cd ${dir}; img build -f ${cicd.job.environment.dockerFile} -t ${imageName} ."

    } else if (cicd.job.environment.registryType == 'imagestream') {
      log.info("[" + cicd.job.environment.container.buildingTool + "] jenkins-builder - Publish Image - ImageStream Registry")
      sh "oc login https://${cicd.job.environment.registryUrlLogin} --token=$pass"
      sh "img login -u \$(oc whoami -t) -p \$(oc whoami -t) ${cicd.job.environment.registryUrlLogin}"
      sh "cd ${dir}; img build -f ${cicd.job.environment.dockerFile} -t ${imageName} ."
    }
  }
}
