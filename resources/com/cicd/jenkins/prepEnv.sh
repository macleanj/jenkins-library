#!/bin/bash
# Main script to be triggered by the Jenkins pipeline
# Required Environment:
# - WORKSPACE: Workspace of the build (usually provided by Jenkins)

programName=$(basename $0)
programDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
baseName=$(echo ${programName} | sed -e 's/.sh//g')

source $programDir/$baseName.conf

# Calculate tag directives
$programDir/triggerByTag.sh "$@"

# Convert configurations to desired format
$programDir/confConvert.sh
