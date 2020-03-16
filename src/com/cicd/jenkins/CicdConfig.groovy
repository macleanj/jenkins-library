package com.cicd.jenkins

class CicdConfig {
  CicdConfig () {
  }

  def get() {
    filename = "com.cicd.jenkins.CicdConfig.properties"
    echo "DEBUG: loading filename: $filename"
    env_string = libraryResource filename
    echo "DEBUG: properties for build:\n$env_string"

    Properties props = new Properties()
    props.load(new ByteArrayInputStream(env_string.getBytes()))
    return props
  }
}