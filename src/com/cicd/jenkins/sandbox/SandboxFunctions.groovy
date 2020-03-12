/*
 * Generic class to develop and test shared libraries
 **/

package com.cicd.jenkins.sandbox

class SandboxFunctions {
    // --- Definitions

    // --- Constructor
    SandboxFunctions() {
    }

    // --- Function
    // Remove all undesired characters
    def cleanString(Map opts = [:]) {
        def x = opts.text.toLowerCase().replaceAll("[^a-z0-9]", "")
        return x
    }

    // --- Comment
}
