package com.cicd.jenkins.utils.maps

class MapMerge {

  /**
    Source: https://gist.github.com/robhruska/4612278
    * Deeply merges the contents of each Map in sources, merging from
    * "right to left" and returning the merged Map.
    *
    * Mimics 'extend()' functions often seen in JavaScript libraries.
    * Any specific Map implementations (e.g. TreeMap, LinkedHashMap)
    * are not guaranteed to be retained. The ordering of the keys in
    * the result Map is not guaranteed. Only nested maps will be
    * merged; primitives, objects, and other collection types will be
    * overwritten.
    *
    * The source maps will not be modified.
    */
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