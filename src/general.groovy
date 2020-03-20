#!/usr/bin/env groovy
// Testing everything related to regular expressions
import static groovy.json.JsonOutput.*

// Testing clone with and without reference
def t1 = 1
if (t1 == 1) {
  def cicd = [job: [:], config: [agent: [:]] ]
  cicd.config.agent.k8 = 'base+jenkins_builder+s_micro'
  cicd.job.agent = cicd.config.agent.k8
  cicd.job.agent = 'base'
  println "cicd: " + prettyPrint(toJson(cicd))
}