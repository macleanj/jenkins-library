// Example for loadLibs. Not used!!
package com.cicd.jenkins.sandbox

import com.cicd.jenkins.sandbox.*

class LoadLibs {
    LoadLibs() {
    }

    def execSandbox(Map opts = [:]) {
      def sb = new SandboxFunctions()
      return sb.example(opts)
    }
}
