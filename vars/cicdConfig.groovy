def call(project, service) {

  // def props = libraryResource("com/cicd/${project}/${service}.yaml")

  def filename = "com/cicd/${project}/${service}.yaml"
  println "DEBUG: loading filename: $filename"

  // Start getting yaml as (nested) object
  def objects = readYaml (file: filename)
  // END getting yaml as (nested) object

  // Start getting properties
  def env_string = libraryResource(filename)
  println "DEBUG: properties for build:\n$env_string"

  Properties props = new Properties()
  props.load(new ByteArrayInputStream(env_string.getBytes()))
  // End getting properties

  return [objects, props]
}
