/**
Default configuration definitions
Defaults:
- globalVars.buildThrottle = 1

*/

def call() {
  def globalVars = [:] 
  globalVars.buildThrottle = 1

  return globalVars
}