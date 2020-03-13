/*
 * Pipeline for creating jenkins images.
  * Examples:
  * - jenkins-jnlp
  * - jenkins-builder
  * - jenkins-helper-nodejs10.x
 **/
import com.cicd.jenkins.tag.*

def call(env) {
  println "Builder pipeline started"
  // env.each { println "$it.key = $it.value" }

  def tagInfo = new TagInfo(env)
  def result = tagInfo.get()
  result.each { println "$it.key = $it.value" }
}
