def processTemplate(context, file) {
    context.openshift.withCluster() {
        def models = context.openshift.process(
            context.readFile(file),
            '--ignore-unknown-parameters=true',
            '-p', 'NAME=test',
            '-p', 'NAMESPACE=test',
            '-p', 'IMAGE_STREAM=test',
            '-p', 'S2I=test',
            '-p', 'CONFIG_MAP=test')
        assert(models)
    }
}

pipeline {
    agent any
    stages {
        stage('Load Shared Library') {
            steps {
                script {
                    def lib = library(
                        identifier: 'wltp-sim-jenkins-local@' + env.GIT_COMMIT,
                        retriever: legacySCM(scm)).com.daimler

                    assert(lib.openshift.OpenShiftBuilder)
                    assert(lib.openshift.OpenShiftDeployment)
                    assert(lib.github.Github)
                }
            }
        }

        stage('Verify OpenShift Templates') {
            steps {
                dir('resources/com/daimler/openshift') {
                    script {
                        processTemplate(this, 'BuildConfig.yml')
                        processTemplate(this, 'DeploymentConfig.yml')
                    }
                }
            }
        }
    }
}
