#!/usr/bin/env groovy
import com.cicd.jenkins.utils.maps.MapUtils
import static groovy.json.JsonOutput.*

// Testing clone with and without reference
def t1 = 1
if (t1 == 1) {
  def cicd = [job: [agent: [:]], config: [agent: [k8: [:]]] ]
  cicd.config.agent.name = 'base+jenkins_builder+s_micro'
  cicd.config.agent.label = 'jnlp'
  cicd.config.agent.cloud = 'kubernetes'
  
  // Direct copy of Map/Object will create a reference
  // cicd.job.agent = cicd.config.agent
  // cicd.job.agent.name = 'base'

  // Individual copy of keys will create a real copy (some kind of work-around)
  // cicd.job.agent.name = cicd.config.agent.name
  // cicd.job.agent.label = cicd.config.agent.label
  // cicd.job.agent.cloud = cicd.config.agent.cloud
  // cicd.job.agent.name = 'base'
  
  // Cloning
  // cicd init above
  def cicdCopy = MapUtils.deepCopy(cicd)
  cicdCopy.config.agent.name = 'base'


  println "cicd: \n" + prettyPrint(toJson(cicd))
  println "cicdCopy: \n" + prettyPrint(toJson(cicdCopy))
}



        // // Job management
        // cicd.job.agent = cicd.config.agent
        // if (env.BUILD_NUMBER.toInteger() > cicd.job.throttle) {
        //   cicd.job.enabled = 0           // Disable staged
        //   cicd.job.agent.name = 'base-disabled'   // Consume as minimal resources as possible.
        // }
