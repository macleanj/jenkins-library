def call(context) {
  def cicd = context.cicd
  def log = context.log

  if (cicd.job.environment.container.buildingTool == 'img') {
    log.info("[" + cicd.job.environment.container.buildingTool + "] jenkins-builder - Build Image")
    sh "img build -f ${cicd.job.environment.dockerFile} -t ${cicd.job.environment.registryUrl}/${cicd.job.environment.registrySpace}/${cicd.appName}:${cicd.tag.versionId} ."
  }
}
`