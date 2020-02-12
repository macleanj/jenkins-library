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
class OpenShiftBuilder {
    // --- Resources
    def buildConfig
    def deploymentConfig

    // --- Data
    def context
    def appName
    def s2iNamespace
    def s2iName
    def imageName
    def imageNamespace
    def branchName
    def tagName
    def deployedFrom
    def configMap
    def internalService

    // --- Resources
    def containerPort
    def replicaCount
    def memoryLimit
    def memoryRequest
    def buildMemoryLimit
    def routerTimeout

    // --- Constructor
    OpenShiftBuilder(context) {
        this.context = context

        this.appName = context.env.NAME
        this.branchName = context.env.GIT_BRANCH
            .replace('origin/', '')
            .replace('/', '-')
            .toLowerCase()
        this.s2iName = context.env.S2I
        this.s2iNamespace = context.env.S2I_NAMESPACE ?: 'ci'
        this.imageName = context.env.IMAGE_STREAM ?: this.appName
        this.imageNamespace = context.env.IMAGE_STREAM_NAMESPACE ?: 'ci'
        this.configMap = context.env.NAME + '-config'

        // For simpler cleanups, we'll label PR deployments as such
        if (context.env.CHANGE_ID) {
            this.deployedFrom = 'pullRequest'
        } else if (this.branchName.equals('master')) {
            this.deployedFrom = 'master'
        } else {
            this.deployedFrom = 'branch'
        }

        // To leverage incremental builds, we need to have a stable tag.
        if (!this.branchName.equals('master')) {
            this.tagName = this.branchName
        } else {
            this.tagName = context.env.GIT_COMMIT.take(8)
        }

        this.internalService = (context.env.INTERNAL_SERVICE ?: 'false').equals('true');
        this.containerPort = context.env.CONTAINER_PORT ?: '8080'
        this.memoryLimit = context.env.MEMORY_LIMIT ?: '256Mi'
        this.memoryRequest = context.env.MEMORY_REQUEST ?: '128Mi'
        this.buildMemoryLimit  = context.env.BUILD_MEMORY_LIMIT ?: '1Gi'
        this.replicaCount = context.env.REPLICA_COUNT ?: '1'
        this.routerTimeout = context.env.ROUTER_TIMEOUT ?: '3m'

        this.buildConfig = context.libraryResource(
            'com/daimler/openshift/BuildConfig.yml')

        this.deploymentConfig = context.libraryResource(
            this.internalService
                ? 'com/daimler/openshift/DeploymentConfigInternal.yml'
                : 'com/daimler/openshift/DeploymentConfig.yml')

        context.echo "Builder initialized for ${this.appName}:${this.branchName}:${this.tagName}"
    }

    // --- Build Logic
    def build() {
        context.openshift.withCluster() {
            def models = context.openshift.process(
                buildConfig,
                '-p', 'NAME=' + this.appName,
                '-p', 'S2I_NAMESPACE=' + this.s2iNamespace,
                '-p', 'S2I=' + this.s2iName,
                '-p', 'IMAGE_STREAM_NAMESPACE=' + this.imageNamespace,
                '-p', 'IMAGE_STREAM=' + this.imageName,
                '-p', 'IMAGE_STREAM_TAG=' + this.tagName,
                '-p', 'BUILT_FROM=' + this.deployedFrom,
                '-p', 'BUILD_MEMORY_LIMIT=' + this.buildMemoryLimit,
                '-p', 'BUILD_SUFFIX=-' + this.branchName)

            context.echo "Build models: ${models}"

            def s = context.openshift.apply(models)
            def selector = s.narrow('bc')
            context.timeout(context.env.BUILD_TIMEOUT ?: 10) {
                def build = selector.startBuild('--from-repo=.')
                selector.logs('-f');
            }

            if (this.deployedFrom.equals('master')) {
                def imageBase =  this.imageNamespace + '/' + this.imageName;
                context.openshift.tag(
                    imageBase + ':' + this.tagName,
                    imageBase + ':latest'
                );
            }

            return s
        }
    }

    // --- Deployment Logic
    def deploy(project) {
        context.openshift.withCluster() {
            context.openshift.withProject(project) {
                def models = context.openshift.process(
                    deploymentConfig,
                    '-p', 'NAME=' + this.appName,
                    '-p', 'NAMESPACE=' + project,
                    '-p', 'IMAGE_STREAM_NAMESPACE=' + this.imageNamespace,
                    '-p', 'IMAGE_STREAM=' + this.imageName,
                    '-p', 'IMAGE_STREAM_TAG=' + this.tagName,
                    '-p', 'DEPLOYED_FROM=' + this.deployedFrom,
                    '-p', 'CONTAINER_PORT=' + this.containerPort,
                    '-p', 'REPLICA_COUNT=' + this.replicaCount,
                    '-p', 'ROUTER_TIMEOUT=' + this.routerTimeout,
                    '-p', 'MEMORY_LIMIT=' + this.memoryLimit,
                    '-p', 'MEMORY_REQUEST=' + this.memoryRequest,
                    '-p', 'DEPLOY_SUFFIX=-' + this.branchName,
                    '-p', 'CONFIG_MAP=' + this.configMap)

                context.echo "Deployment models: ${models}"
                def s = context.openshift.apply(models)
                def targetUrl = ''

                if (!this.internalService) {
                    context.echo "Generating external URL ..."
                    targetUrl = 'http://' + s.narrow('route').object().spec.host
                    context.echo "External URL: ${targetUrl}"
                }

                context.echo "Rolling out deployment ..."
                s.narrow('dc').rollout().latest()

                return new OpenShiftDeployment(context, s, project, targetUrl)
            }
        }
    }
}
