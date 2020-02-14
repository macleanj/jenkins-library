#!/bin/bash
# Main script to be triggered by the Jenkins pipeline

programDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $programDir/$baseName.conf

# Calculate tag directives
$programDir/triggerByTag.sh "$@"

# Convert configurations to desired format
$programDir/confConvert.sh
