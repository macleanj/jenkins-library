package com.cicd.jenkins.pipeline

/*
 *
 * Tigger by tag examples, Character extraction by expected format:
 * - Build Single : $buildTag<imageTypeKey>-<version>. Example: bh-1.01
 * - Build Multi  : $buildTag<imageTypeKey>-<appName>-<version>. Example: bv-clojure-2.8.1
 * - Deploy       : $deployTag<imageTypeKey>-<deployEnvironment>-<version>. Example: dv-prod-1.01
 */

class PipelineUtils {
  // --- Resources
  def context
  def cicd
  def log
  
  // --- Constructor
  PipelineUtils(context) {
    this.context = context
    this.cicd = context.cicd
    this.log = context.log
  }

  // --- Method Logic
  def build() {
    if (cicd.job.environment.container.buildingTool == 'img') {
      log.info("[" + cicd.job.environment.container.buildingTool + "] " + cicd.job.environment.container.builder + " - Build Image")
      "img build -f ${cicd.job.environment.dockerFile} -t ${cicd.job.environment.registryUrl}/${cicd.job.environment.registrySpace}/${cicd.appName}:${cicd.tag.versionId} .".exec()
    }
    // return
  }
}
