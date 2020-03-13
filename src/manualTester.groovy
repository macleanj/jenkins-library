#!/usr/bin/env groovy

/*
 * Script to test and develop libraries
 **/

// Definitions
def context  =  [:]
context.env  =  [:]
def shell

/*************************************
DEBUG: Environment
ALT_DOCKER_REGISTRY = https://repository.mac-lean.com:10000
ALT_REPOSITORY = repository.mac-lean.com
APP_NAME = hello-world
BRANCH_NAME = bv-1.55
BUILD_DISPLAY_NAME = #1
BUILD_ID = 1
BUILD_NUMBER = 1
BUILD_TAG = jenkins-trigger-by-tag-poc-trigger-by-tag-bv-1.55-1
BUILD_URL = http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/1/
CICD_BUILD_ENABLED = 1
CICD_CLOUD_REGISTRY = Kubernetes
CICD_CLOUD_TYPE = Kubernetes
CICD_DEBUG = 1
CICD_DEPLOY_ENABLED = 0
CICD_REGISTRY_CRED_ID = jenkins-sa
CICD_REGISTRY_SPACE = ci
CICD_REGISTRY_URL = registry.pro-eu-west-1.openshift.com
CICD_TAGS_ID = 1.55
CICD_TAGS_IMAGE_TYPE = version
CICD_TAGS_TAG_TYPE = build
COPY_REFERENCE_FILE_LOG = /var/jenkins_home/copy_reference_file.log
EXECUTOR_NUMBER = 0
GIT_AUTHOR_NAME = Jerome Mac Lean
GIT_BRANCH = bv-1.55
GIT_COMMIT = c157499dd7b489c6c15714ad97c8540b25444d9d
GIT_URL = https://github.com/macleanj/hello-world.git
HOME = /var/jenkins_home
HOSTNAME = jenkins-master
HTTP_PORT = 8082
HUDSON_COOKIE = a2f9b0ac-838e-4931-bdcf-4298da82682b
HUDSON_HOME = /var/jenkins_home
HUDSON_SERVER_COOKIE = 6aa91881e6bea1a3
HUDSON_URL = http://jenkins-master.lab.crosslogic-consulting.com:8082/
JAVA_BASE_URL = https://github.com/AdoptOpenJDK/openjdk8-upstream-binaries/releases/download/jdk8u242-b08/OpenJDK8U-jdk_
JAVA_HOME = /usr/local/openjdk-8
JAVA_OPTS = -Xmx2048m
JAVA_URL_VERSION = 8u242b08
JAVA_VERSION = 8u242
JENKINS_HOME = /var/jenkins_home
JENKINS_INCREMENTALS_REPO_MIRROR = https://repo.jenkins-ci.org/incrementals
JENKINS_NODE_COOKIE = bdf6aa02-6abb-441d-96e9-27cb999cc491
JENKINS_SERVER_COOKIE = durable-60a85e4dbfeab7aeffd390e4444bce96
JENKINS_SLAVE_AGENT_PORT = 50000
JENKINS_UC = https://updates.jenkins.io
JENKINS_UC_EXPERIMENTAL = https://updates.jenkins.io/experimental
JENKINS_URL = http://jenkins-master.lab.crosslogic-consulting.com:8082/
JENKINS_VERSION = 2.219
JOB_BASE_NAME = bv-1.55
JOB_DISPLAY_URL = http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/display/redirect
JOB_NAME = trigger-by-tag/poc-trigger-by-tag/bv-1.55
JOB_URL = http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/
LANG = C.UTF-8
NODE_LABELS = master
NODE_NAME = master
PATH = /usr/local/openjdk-8/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
PREP_LOAD_ENV = null
PWD = /var/jenkins_home/workspace/y-tag_poc-trigger-by-tag_bv-1.55
REF = /usr/share/jenkins/ref
RUN_CHANGES_DISPLAY_URL = http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/1/display/redirect?page = changes
RUN_DISPLAY_URL = http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/1/display/redirect
SHLVL = 0
STAGE_NAME = Set environment
TAG_DATE = Fri Mar 13 17:25:38 UTC 2020
TAG_NAME = bv-1.55
TAG_TIMESTAMP = 1584120338000
TAG_UNIXTIME = 1584120338
TMP_TAGS_NAME = bv-1.55
WORKSPACE = /var/jenkins_home/workspace/y-tag_poc-trigger-by-tag_bv-1.55
WORKSPACE_BASE_DIR = y-tag_poc-trigger-by-tag_bv-1.55
WORKSPACE_LIBS = /var/jenkins_home/workspace/y-tag_poc-trigger-by-tag_bv-1.55/../y-tag_poc-trigger-by-tag_bv-1.55@libs
*************************************/

context.env.BRANCH_NAME = "bv-1.55"
context.env.BUILD_NUMBER = "1"
context.env.BUILD_URL = "http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/1/"
context.env.GIT_AUTHOR_NAME = "Jerome Mac Lean" // (created)
context.env.GIT_BRANCH = "bv-1.55"
context.env.GIT_COMMIT = "c157499dd7b489c6c15714ad97c8540b25444d9d"
context.env.GIT_URL = "https://github.com/macleanj/hello-world.git"
context.env.HOME = "/var/jenkins_home"
context.env.HOSTNAME = "jenkins-master:"
context.env.JENKINS_HOME = "/var/jenkins_home"
context.env.JENKINS_URL = "http://jenkins-master.lab.crosslogic-consulting.com:8082/"
context.env.JENKINS_VERSION = "2.219"
context.env.JOB_BASE_NAME = "bv-1.55"
context.env.JOB_DISPLAY_URL = "http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/display/redirect"
context.env.JOB_NAME = "trigger-by-tag/poc-trigger-by-tag/bv-1.55"
context.env.JOB_URL = "http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/"
context.env.PWD = "/var/jenkins_home/workspace/y-tag_poc-trigger-by-tag_bv-1.55"
context.env.RUN_CHANGES_DISPLAY_URL = "http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/1/display/redirect?page = changes"
context.env.RUN_DISPLAY_URL = "http://jenkins-master.lab.crosslogic-consulting.com:8082/job/trigger-by-tag/job/poc-trigger-by-tag/job/bv-1.55/1/display/redirect"
context.env.TAG_NAME = "bv-1.55"
context.env.WORKSPACE = "/var/jenkins_home/workspace/y-tag_poc-trigger-by-tag_bv-1.55"

// Executing the script
shell  =  new GroovyShell()
shell.parse(new File('../vars/pipelineBuilders.groovy')).call(context)
