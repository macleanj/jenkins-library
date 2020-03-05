def call() {
  def globalVars = [:]
  globalVars.buildThrottle = 1
  return globalVars
}

// class globalVars {
//   final Integer buildThrottle = 1
//   // final String SUCCESS = "SUCCESS"
//   // final String FAILURE = "FAILURE"
//   // final String ABORTED = "ABORTED"
// }
