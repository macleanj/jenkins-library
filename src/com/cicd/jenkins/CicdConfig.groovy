package com.cicd.jenkins

class CicdConfig {
  CicdConfig () {
  }

  def get() {
    def filename = 'com/cicd/jenkins/CicdConfig.yaml'
    println "DEBUG: loading filename: $filename"
    def env_string = libraryResource 'com/cicd/jenkins/CicdConfig.yaml'
    // def env_string = libraryResource filename
    // def env_string = sh(libraryResource(filename.toString()))
    println "DEBUG: properties for build:\n$env_string"

    // Properties props = new Properties()
    // props.load(new ByteArrayInputStream(env_string.getBytes()))
    // return props
  }
}