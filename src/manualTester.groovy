#!/usr/bin/env groovy

/*
 * Script to test and develop libraries
 **/

// Definitions
def context = [:]
context.env = [:]
def shell

// Executing the script
context.env.APP_NAME = "app_name"
// shell = new GroovyShell()
// shell.parse(new File('../vars/pipelineBuilders.groovy' + file)).call()

println context.env.APP_NAME