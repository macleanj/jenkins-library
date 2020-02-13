
package com.daimler.openshift

import java.io.File 

// ----------------------------------------------------
// Environment Preperation Logic
// * relies on the trigger by tag methodology
//
// Required Environment:
// * WORKSPACE: The workspace of te Jenkins build (usually provided by Jenkins)
// ----------------------------------------------------

class OpenShiftEnvironment {
  // --- Resources
  def workspace
  def workspace_lib
  // def envGeneric

  // --- Data
  def prepTags

  // --- Constructor
  OpenShiftEnvironment(context) {
    this.workspace = context.env.WORKSPACE
    this.workspace_lib = "${this.workspace}/../workspace@libs/cicd-daimler-wltp-sim"
    // this.envGeneric = "${this.workspace_lib}/resources/com/cicd/jenkins/env.files/generic.groovy"
    this.prepTags = "${this.workspace_lib}/resources/com/cicd/jenkins/_confConvert.sh bv-1.00 1a2b3c4d"


    // context.echo "Environment: ${this.workspace}"
    this.prepTags.execute()

    new File("${this.workspace_lib}/resources/com/cicd/jenkins/env.files/generic.groovy").eachLine {  
      line -> println "line : $line"; 
    } 
    
    // load "${this.workspace_lib}/resources/com/cicd/jenkins/env.files/generic.groovy"
    // load ( "${this.workspace}/../workspace@libs/cicd-daimler-wltp-sim/resources/com/cicd/jenkins/env.files/tag_env.groovy"toString() )
    }
  }