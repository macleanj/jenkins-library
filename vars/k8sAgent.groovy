#!/usr/bin/env groovy

/*
 * Kubernetes agent definition based on file-reference. The yml files referenced will merged to build one single agent yaml file.
 * Jerome Mac Lean - CrossLogic Consulting <jerome@crosslogic-consulting.com>
 */

import com.cicd.jenkins.utils.yaml.YamlMerge
import com.cicd.jenkins.utils.logging.LogLevel
import com.cicd.jenkins.utils.logging.Logger

def call(context) {
  Logger.init(context, [ logLevel: LogLevel[context.env.CICD_LOGLEVEL] ])
  def log = new Logger(this)
  def opts = context.cicd.job.environment.agent

  // name is in a format of a+b+c, so the content will be added together from resource
  // example: small+pg we will collect the content from base.yaml, small.yaml, pg.yaml
  String name = opts.get('name', 'base')
  String defaultLabel = "${name.replace('+', '_')}-${UUID.randomUUID().toString()}"
  String label = opts.get('label', defaultLabel)
  String cloud = opts.get('cloud', 'kubernetes')
  def ret = [:]

  def comps = name.split('\\+|-').toList()

  def templates = []
  String template
  for (c in comps) {
    if (fileExists('config/podtemplates/' + c + '.yaml')) {
      // Take application templates
      template = readFile('config/podtemplates/' + c + '.yaml')
      log.trace("Agent: Specific template: " + template)
    } else {
      // Take global templates
      template = k8sAgentGlobalTemplates(c)
      log.trace("Agent: Generic template: " + template)
    }
    templates.add(template)
  }

  def myyaml = new YamlMerge()
  def final_template = myyaml.merge(templates)

  ret['cloud'] = cloud
  ret['label'] = label
  ret['yaml'] = final_template

  return ret
}
