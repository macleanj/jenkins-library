
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
  // Prepare environment
  def sout = new StringBuilder(), serr = new StringBuilder()
  def proc = '${JENKINS_PATH}/build/config/_confConvert.sh bv-1.00 1a2b3c4d'.execute()
  proc.consumeProcessOutput(sout, serr)
  proc.waitForOrKill(1000)
  println "out> $sout err> $serr"
}
