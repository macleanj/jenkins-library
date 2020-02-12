# daimler-wltp-sim-jenkins

[Jenkins][jenkins] is a flexible and extensible CI/CD environment. It allows for
build pipelines to be defined in a declarative way, contained in the so-called
`Jenkinsfile`.

We are running out Jenkins instance on [OpenShift][openshift], providing this
repository as a shared library to all Jenkins jobs.

[jenkins]: https://jenkins.io
[openshift]: https://openshift.com

## Setup

### Master

For this, we used the `jenkins-persistent` template that's provided by OpenShift
by default. The following settings seem to result in reasonable responsiveness:

__Memory__
1GiB

__Persistent Storage__
1.5GiB

__Environment Adjustments__

```
# Allow generation of HTML reports (requires disabling HTTP sandboxing)
JENKINS_JAVA_OPTIONS="-Djava.awt.headless=true -Dhudson.model.DirectoryBrowserSupport.CSP="
```

### Agents

We added the following agents to our OpenShift CI project, allowing them to be
instantiated on-demand:

- [NodeJS 8 LTS][nodejs8-agent]
- [Clojure][clojure-agent]

[nodejs8-agent]: https://github.com/futurice/openshift-templates/tree/master/jenkins/nodejs8-agent
[clojure-agent]: https://github.com/futurice/openshift-templates/tree/master/jenkins/clojure-agent

### Plugins

The following plugins were added to provide extended functionality:

- [Pipeline: Github][plugin-github-pipeline]

[plugin-github-pipeline]: https://github.com/jenkinsci/pipeline-github-plugin

### Project Configuration

We created one folder in Jenkins named `daimler-wltp-simulation`, with
multibranch pipelines for `client`, `server`, etc...

The [shared library][shared] is configured on the folder level and named
`wltp-sim-jenkins`, pointing at this repository.

[shared]: https://jenkins.io/doc/book/pipeline/shared-libraries/

## GitHub

### Machine User

We set up a GitHub machine user called [we-like-team-play][wltp] and gave it
write access to our repositories (but not the whole organisation) by adding it
as a __collaborator__. The account needs __write access__ to be able to
communicate build results back to GitHub.

Futurice requires 2FA, so this user is currently connected to an instance of
Google Authenticator on Yannick's phone. Recovery codes and the private SSH key
will be put in the Futurice password safe.

[wltp]: https://github.com/we-like-team-play

### BlueOcean

We used the BlueOcean UI to set up the connection between Jenkins and Github.
Unfortunately, this UI does not show repositories that the user has access to if
they are part of an organisation the user does not belong to.

So, we worked around that by setting up the pipeline for a dummy repository and
replaced it after-the-fact within the classic Jenkins UI.

### Webhook

In the repository, go to `Settings -> Webhooks -> Add Webhook` and provide the
URL to your Jenkins instance, suffixed with `/github-webhook/`. Make sure to
select the JSON content type and delivery of all events.

This will cause builds to be triggered on a variety of GitHub events.
