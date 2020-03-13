/*
 * This is a test pipeline that can be (copied and) used to a Jenkins pipeline project as PoC.
 **/

// This library with the branch to test
library identifier: 'jenkins-lib-pipeline@develop', retriever: modernSCM (
  [ $class: 'GitSCMSource',
    remote: 'https://github.com/macleanj/jenkins-library.git',
    credentialsId: 'futurice-maclean-github'
  ]
 )

// The filename in vars/ (e.g. Sandbox.groovy)
PipelineBuilders()