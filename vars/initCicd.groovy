import com.cicd.jenkins.CicdConfig

def call() {
  // https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readyaml-read-yaml-from-files-in-the-workspace-or-text

  // def cicd = [build: [:], git: [:], jenkins: [:], config: [:], env: [:]]
  // cicd.build.debug = 1
  // cicd.build.throttle = 1
  // cicd.build.number = currentBuild.getNumber()

  node ('master') {
    stage('Initialize CICD') {
      sh 'echo "master - Stage: Initialize CICD"'
      def cicdConfig = new CicdConfig()
      def cicd = cicdConfig.get()
      println cicd
      if (cicd.build.debug == 1) { echo "DEBUG: CICD Environment\n" + sh(script: "printenv | sort", returnStdout: true) }
    }
  }

  return cicd
}