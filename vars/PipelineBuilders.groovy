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

  def tagInfo = new TagInfo()
  result = tagInfo.get()
  println "My result: " + result
}

// Testing
def result = this.call(text: "a_B-c.1")
println result