def getcicdConfig(project, service) {
def filename = "com/cicd/jenkins/CicdConfig.yaml"
println "DEBUG: loading filename: $filename"
def env_string = libraryResource filename
println "DEBUG: properties for build:\n$env_string"

Properties props = new Properties()
props.load(new ByteArrayInputStream(env_string.getBytes()))
return props
}
