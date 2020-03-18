#!/usr/bin/env groovy
// Testing everything related to regular expressions
import static groovy.json.JsonOutput.*
import groovy.json.JsonSlurper
import groovy.yaml.YamlSlurper

// Testing only
def yamlSlurper = new YamlSlurper()
def cicd = yamlSlurper.parseText '''
# Example for development only
loglevel: 'INFO'
job:
  throttle: 1
build:
  buildEnvironment: 'dev'
pr:
  buildEnabled: 1
  deployEnabled: 0
  buildEnvironment: 'test'
deploy:
    dev:
      platformName: 'dev Kubernetes or OpenShift'
      platformOwner: 'Owner'
      platformNamespace: 'ns-dev'
    test:
      platformName: 'test Kubernetes or OpenShift'
      platformOwner: 'Owner'
      platformNamespace: 'ns-test'
    stag:
      platformName: 'stag Kubernetes or OpenShift'
      platformOwner: 'Owner'
      platformNamespace: 'ns-stag'
    prod:
      platformName: 'prod Kubernetes or OpenShift'
      platformOwner: 'Owner'
      platformNamespace: 'ns-prod'
'''
// println "cicd: " + prettyPrint(toJson(cicd))
// println cicd.deploy.keySet()

def git = [:]
def TriggerByTagConstants = [:]

// Correct
git.gitHash = "123456-long"
git.gitHashShort = git.gitHash ? git.gitHash.take(6) : ''
// git.changeId = "PR-123"
// git.tagName = "bh-1.86"
// git.tagName = "bv-1.86"
// git.tagName = "bh-my_app_from_builders-1.86"
// git.tagName = "bv-my_app_from_builders-1.86"
// git.tagName = "dh-prod-1.86"
// git.tagName = "dv-prod-1.86"

// Not correct
// git.tagName = "xv-1.86"
// git.tagName = "dx-1.86"
// git.tagName = "bv-app-Name-1.86"
// git.tagName = "bv-Naa-1.86"
// git.tagName = "dv-aaa-1.86"

TriggerByTagConstants.buildTag = "b"
TriggerByTagConstants.deployTag = "d"
TriggerByTagConstants.versionTag = "v"
TriggerByTagConstants.hashTag = "h"

    if (git.changeId) {
      git.triggerType = 'pullRequest'
    } else if (git.tagName) {
      git.triggerType = 'tag'
    } else {
      git.triggerType = 'unknown'
    }

    if (1 == 1) {







      // START VALIDATION
      if (git.triggerType == 'tag') { 
        cicd.job.enabled = 0
        // Only supported patterns will match
        def TagNamePattern="^([${TriggerByTagConstants.buildTag}${TriggerByTagConstants.deployTag}])([${TriggerByTagConstants.versionTag}${TriggerByTagConstants.hashTag}])-([a-z0-9._]+)[-]*([0-9.]*)\$"
        def tagNameArray = (git.tagName =~ /${TagNamePattern}/)

        if (tagNameArray) {
          // Overall allowed tagPattern is matched
          println prettyPrint(toJson(tagNameArray[0]))
          def tagTypeKey = tagNameArray[0][1]
          def versionKey = tagNameArray[0][2]
          def partTwo = tagNameArray[0][3]
          def partThree = tagNameArray[0][4]

          // Build instantiation
          if (tagTypeKey == TriggerByTagConstants.buildTag) {
            git.tagType="build"
            // log.info("Tag:" + git.tagType)
            println "Tag: " + git.tagType
            if (partThree ==~ /[0-9.]+/) {
              // log.info("Tag: Build Multi")
              println "Tag: Build Multi"
              if (partTwo ==~ /[a-z_]+/) {
                cicd.job.enabled = 1
                cicd.job.buildEnabled = 1
                cicd.job.deployEnabled = 0
                git.appName = partTwo.toLowerCase().replaceAll("[_]", "-")

                // Set version
                git.versionId = (versionKey == TriggerByTagConstants.versionTag) ? partThree : git.gitHashShort

                // Used environment mapping
                cicd.job.environment = cicd.deploy[cicd.build.buildEnvironment]
              } else {
                // log.error("Tag: " + git.tagType + " tag not valid - bad appName pattern")
                println "Tag: " + git.tagType + " tag not valid - bad appName pattern"
              }
            } else if (partTwo ==~ /[0-9.]+/) {
              // TODO: add enableBuild after perfectly matching pattern
              // log.info("Tag: Build Single")
              println "Tag: Build Single"
              cicd.job.enabled = 1
              cicd.job.buildEnabled = 1
              cicd.job.deployEnabled = 0

              // Set version
              git.versionId = (versionKey == TriggerByTagConstants.versionTag) ? partTwo : git.gitHashShort

              // Used environment mapping
              cicd.job.environment = cicd.deploy[cicd.build.buildEnvironment]
            } else {
              // log.error("Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern")
              println "Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern"
            }

          // Deployment instantiation
          } else if (tagTypeKey == TriggerByTagConstants.deployTag) {
            git.tagType="deployment"
            // log.info("Tag:" + git.tagType)
            println "Tag:" + git.tagType
            if (partTwo ==~ /[a-z_]+/ && partThree ==~ /^[0-9.]+$/) {
              git.envKey = partTwo
              if (cicd.deploy.containsKey(git.envKey)) {
                cicd.job.enabled = 1
                cicd.job.buildEnabled = 1
                cicd.job.deployEnabled = 1

                // Set version
                git.versionId = (versionKey == TriggerByTagConstants.versionTag) ? partThree : git.gitHashShort

                // Used environment mapping
                cicd.job.environment = cicd.deploy[git.envKey]
              } else {
                // log.error("Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern")
                println "Tag: " + git.tagType + " tag not valid - not supported environment"
              }
            } else {
              // log.error("Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern")
              println "Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern"
            }
          
          // No "else" needed here as errors are caught in the initial pattern check

          }

        } else {
            git.tagType="overall"
            // log.error("Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern")
            println "Tag: " + git.tagType + " tag not valid - bad " + git.tagType + " tag pattern"
        }
      // Tag
      
      } else if (git.triggerType == "pullRequest") { 
        cicd.job.enabled = 1
        cicd.job.buildEnabled = cicd.pr.buildEnabled
        cicd.job.deployEnabled = cicd.pr.deployEnabled
        // log.info("pullRequest")
        println "pullRequest"

        // Set version
        git.versionId = git.gitHashShort

        // Used environment mapping
        cicd.job.environment = cicd.deploy[cicd.pr.buildEnvironment]
      } else { 
        // log.error("Unknown trigger")
        println "Unknown trigger"
      }
      // END VALIDATION
    
    
    
    }

cicd.git = git
println "Job enabled: " + cicd.job.enabled
println "cicd: " + prettyPrint(toJson(cicd))
