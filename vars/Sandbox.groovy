/*
 * Sandbox to develop and test the pipeline scripts
 **/
import com.cicd.jenkins.sandbox.*

def call() {
  println "Hello World!"

  def sb = new SandboxFunctions()
  def result

  result = sb.cleanString(text: "a_B-c.1")
  println "My result: " + result
}
