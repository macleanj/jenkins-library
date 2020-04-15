/*
 * Loading the local build and deploy configuration
 * Jerome Mac Lean - CrossLogic Consulting <jerome@crosslogic-consulting.com>
 */
def call(project, service) {
  def debug = 0

  def filename = "com/cicd/${project}/${service}.yaml"
  if (debug == 1) { println "DEBUG: loading filename: $filename" }

  // Getting yaml as (nested) object
  def objs = readYaml text: libraryResource(filename)
  if (debug == 1) { println "DEBUG: objects for build:\n$objs" }

  // Getting properties
  def env_string = libraryResource(filename)
  if (debug == 1) { println "DEBUG: properties for build:\n$env_string" }
  Properties props = new Properties()
  props.load(new ByteArrayInputStream(env_string.getBytes()))

  return [objs, props]
}
