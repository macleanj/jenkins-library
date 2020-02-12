package com.daimler.openshift

class OpenShiftDeployment {
    // --- Data
    def context
    def selector
    def project
    def targetUrl

    // --- Constructor
    OpenShiftDeployment(context, selector, project, targetUrl) {
        this.context = context
        this.selector = selector
        this.project = project
        this.targetUrl = targetUrl
    }

    // --- Deployment
    def verify() {
        context.echo "Verifying deployment to '${this.project}'..."

        context.openshift.withCluster() {
            context.openshift.withProject(project) {
                this.publish('pending', 'Branch is being deployed')

                def dc = this.selector.narrow('dc').object()
                def deploymentName = dc.metadata.name
                context.echo "Finding replication controller for deployment '${deploymentName}..."

                def latestVersion = dc.status.latestVersion
                def rcName = deploymentName + '-' + latestVersion
                def rc = context.openshift.selector('rc', rcName)

                context.echo "Waiting for replication controller '${rcName}..."
                context.timeout(context.env.DEPLOY_TIMEOUT ?: 10) {
                    rc.untilEach(1) {
                        def rcMap = it.object()
                        return (rcMap.status.replicas.equals(rcMap.status.readyReplicas))
                    }
                }
                context.echo "Done"
            }
        }
    }

    def success() {
        this.publish('success', 'Branch was deployed successfully')
    }

    def failure() {
        this.publish('failure', 'Branch could not be deployed.')
    }

    def publish(status, text) {
        if (context.env.CHANGE_ID) {
            context.pullRequest.createStatus(
                status: status,
                context: 'continuous-delivery/jenkins/' + project,
                description: text ?: '',
                targetUrl: this.targetUrl ?: '')
        }
    }
}
