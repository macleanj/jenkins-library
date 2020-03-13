/*
 * Generic class to develop and test shared libraries
 **/

package com.cicd.jenkins.tag

class TagInfo {
  // --- Definitions
  def context
  def result = [:]

  // --- Constructor
  TagInfo(context) {
    this.context = context.env
    this.result = context
  }

  // --- Function
  // Translate tag into information and actions 
  def get() {
    return this.result
  }
}
