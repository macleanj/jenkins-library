/*
 * Pipeline for creating jenkins images.
  * Examples:
  * - jenkins-jnlp
  * - jenkins-builder
  * - jenkins-helper-nodejs10.x
 **/
import com.cicd.jenkins.tag.*

def call(context) {
  println "Builder pipeline started"
  // context.env.each { println "$it.key = $it.value" }

  def tagInfo = new TagInfo(context.env)
  def result = tagInfo.get()
  result.each { println "$it.key = $it.value" }
}
