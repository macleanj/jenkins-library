/**
 * Source: https://gist.github.com/robhruska/4612278
 */
package com.cicd.jenkins.utils.maps

class MapMerge {

  Map merge(Map[] sources) {
    if (sources.length == 0) return [:]
    if (sources.length == 1) return sources[0]

    sources.inject([:]) { result, source ->
      source.each { k, v ->
        result[k] = result[k] instanceof Map ? merge(result[k], v) : v
      }
      result
    }
  }
}