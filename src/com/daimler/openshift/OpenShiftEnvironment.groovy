
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
    def workspace_lib

    // --- Data
    def context
    def prepTags

  // --- Constructor
  OpenShiftEnvironment(context) {
      this.context = context
      this.workspace = context.env.WORKSPACE
      this.workspace_lib = "${this.workspace}/../workspace@libs/cicd-daimler-wltp-sim"
      this.prepTags = "${this.workspace_lib}/resources/com/cicd/jenkins/_confConvert.sh bv-1.00 1a2b3c4d"


      // context.echo "Environment: ${this.workspace}"
      this.prepTags.execute()
      // load ( "${this.workspace}/../workspace@libs/cicd-daimler-wltp-sim/resources/com/cicd/jenkins/env.files/tag_env.groovy"toString() )
  }

  // --- Build Logic
  def loadEnv() {
    load ( "${this.workspace}/../workspace@libs/cicd-daimler-wltp-sim/resources/com/cicd/jenkins/env.files/generic.groovy".toString() )
    return null
  }
}