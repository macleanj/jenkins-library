#!/usr/bin/env groovy
import com.cicd.jenkins.utils.yaml.YamlMerge

def call(Map opts = [:]) {
  // name is in a format of a+b+c, so the content will be added together from resource
  // example: small+pg we will collect the content from base.yaml, small.yaml, pg.yaml

  String name = opts.get('name', 'base')
  String defaultLabel = "${name.replace('+', '_')}-${UUID.randomUUID().toString()}"
  String label = opts.get('label', defaultLabel)
  String cloud = opts.get('cloud', 'kubernetes')
  def ret = [:]

  def comps = name.split('\\+|-').toList()

  // JML: Bug fix.
  // base needs to be explicitly configured
  // if (name != 'base') {
  //   comps = comps.plus(0, 'base')
  // }

  // JML: 
  // def templates = []
  // String template
  // for (c in comps) {
  //   template = libraryResource 'podtemplates/' + c + '.yaml'
  //   templates.add(template)
  // }

  def templates = []
  String template
  for (c in comps) {
    if (assert 'config/podtemplates/' + c + '.yaml'.exists()) {
      // Take application templates
      template = 'config/podtemplates/' + c + '.yaml'
    } else {
      // Take global templates
      template = k8sAgentGlobalTemplates(c)
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
