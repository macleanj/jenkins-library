def call(context) {
  def cicd = context.cicd
  def log = context.log
  def dir = "."

  if (cicd.tag.appName) {
    dir = "./" + cicd.tag.appName
  }

  def imageName = cicd.job.environment.registryUrl + "/" + cicd.job.environment.registrySpace + "/" + cicd.job.environment.appNamePrefix + cicd.appName + cicd.job.environment.appNamePostfix + ":" + cicd.tag.versionId
  if (cicd.job.environment.container.buildingTool == 'img') {
    log.info("[" + cicd.job.environment.container.buildingTool + "] jenkins-builder - Build Image")
    sh "cd ${dir}; img build -f ${cicd.job.environment.dockerFile} -t ${imageName} ."
  }
}

      
      
