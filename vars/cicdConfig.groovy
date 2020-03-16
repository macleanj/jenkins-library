def call(project, service) {

  // def props = libraryResource("com/cicd/${project}/${service}.yaml")

  def filename = "com/cicd/${project}/${service}.yaml"
  println "DEBUG: loading filename: $filename"
  // def env_string = libraryResource filename
  def env_string = libraryResource(filename)
  println "DEBUG: properties for build:\n$env_string"

  Properties props = new Properties()
  props.load(new ByteArrayInputStream(env_string.getBytes()))
  return props
}
