
package com.daimler.openshift

// ----------------------------------------------------
// Build/Deployment Logic
// * relies on the environment to configure itself
// * uses common templates bundled with the library
//
// Required Environment:
// * NAME: Application to build
// * S2I: Builder image to use
// * GIT_BRANCH: Branch to build (usually provided by Jenkins)
// * GIT_COMMIT: Commit to build (usually provided by Jenkins)
// ----------------------------------------------------

class OpenShiftEnvironment {
    // --- Resources
    def workspace

    // --- Data
    def context
    def prepTags

  // --- Constructor
  OpenShiftEnvironment(context) {
      this.context = context
      this.workspace = context.env.WORKSPACE
      this.workspace_lib = "${this.workspace}/workspace@libs"
      this.prepTags = "${this.workspace_lib}/resources/com/cicd/jenkins/_confConvert.sh bv-1.00 1a2b3c4d"


      context.echo "Environment: ${this.workspace}"
      this.prepTags.execute()
      // load ( ${this.workspace}/build/config/env.files/generic.groovy )
      // load ( ${this.workspace}/build/config/env.files/tag_env.groovy )

      // 'printenv | sort'.execute()





      // this.appName = context.env.APP_NAME
      // this.branchName = context.env.GIT_BRANCH
      //     .replace('origin/', '')
      //     .replace('/', '-')
      //     .toLowerCase()
      // this.s2iName = context.env.S2I
      // this.s2iNamespace = context.env.S2I_NAMESPACE ?: 'ci'
      // this.imageName = context.env.IMAGE_STREAM ?: this.appName
      // this.imageNamespace = context.env.IMAGE_STREAM_NAMESPACE ?: 'ci'
      // this.configMap = context.env.NAME + '-config'

      // // For simpler cleanups, we'll label PR deployments as such
      // if (context.env.CHANGE_ID) {
      //     this.deployedFrom = 'pullRequest'
      // } else if (this.branchName.equals('master')) {
      //     this.deployedFrom = 'master'
      // } else {
      //     this.deployedFrom = 'branch'
      // }

      // // To leverage incremental builds, we need to have a stable tag.
      // if (!this.branchName.equals('master')) {
      //     this.tagName = this.branchName
      // } else {
      //     this.tagName = context.env.GIT_COMMIT.take(8)
      // }

      // this.internalService = (context.env.INTERNAL_SERVICE ?: 'false').equals('true');
      // this.containerPort = context.env.CONTAINER_PORT ?: '8080'
      // this.memoryLimit = context.env.MEMORY_LIMIT ?: '256Mi'
      // this.memoryRequest = context.env.MEMORY_REQUEST ?: '128Mi'
      // this.buildMemoryLimit  = context.env.BUILD_MEMORY_LIMIT ?: '1Gi'
      // this.replicaCount = context.env.REPLICA_COUNT ?: '1'
      // this.routerTimeout = context.env.ROUTER_TIMEOUT ?: '3m'

      // this.buildConfig = context.libraryResource(
      //     'com/daimler/openshift/BuildConfig.yml')

      // this.deploymentConfig = context.libraryResource(
      //     this.internalService
      //         ? 'com/daimler/openshift/DeploymentConfigInternal.yml'
      //         : 'com/daimler/openshift/DeploymentConfig.yml')

      // context.echo "Environment initialized for ${this.appName}:${this.branchName}:${this.tagName}"
  }
}
