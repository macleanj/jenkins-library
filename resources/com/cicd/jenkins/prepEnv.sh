#!/bin/bash
# Main script to be triggered by the Jenkins pipeline

programName=$(basename $0)
programDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
baseName=$(echo ${programName} | sed -e 's/.sh//g')

echo "export WORKSPACE=${1}" > WORKSPACE.conf
source WORKSPACE.conf
shift 1
source $programDir/$baseName.conf

# Calculate tag directives
$programDir/triggerByTag.sh "$@"

# Convert configurations to desired format
$programDir/confConvert.sh
