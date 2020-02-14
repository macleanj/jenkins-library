#!/bin/bash
# Script to convert configuration files to environment variable syntax

now=$(date +%Y-%m-%d\ %H:%M:%S)
programName=$(basename $0)
programDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
baseName=$(echo ${programName} | sed -e 's/.sh//g')
debug=0

cd $programDir
source prepEnv.conf
source $baseName.conf

# Continue with conversion of environment files
for file in $(ls $CICD_FILES_DIR/*.conf); do
  [ $debug -eq 1 ] && echo "Processing $file"

  # Get basename of the file
  baseNameFile=$(echo ${file} | sed -e 's/.*\///g' | sed -e 's/.conf//g')
  
  # Prepare file(s) for environment variable format
  # echo -n > ${CICD_FILES_DIR}/${baseNameFile}.env

  # Prepare file(s) for groovy format
  echo -n > ${CICD_FILES_DIR}/${baseNameFile}.groovy

  # Convert
  OLDIFS=$IFS
  IFS=$'\n'
  for env in $(cat $file | egrep -v "^#|^[[:space:]]"); do
    [ $debug -eq 1 ] && echo "RAW  : $env"
    key=$(echo $env | awk -F "=" '{print $1}' | awk '{$1=$1};1')
    value=$(echo $env | sed -e 's/^[-_a-zA-Z0-9]*=//g' | sed -e 's/[#].*//g' | awk '{$1=$1};1')
    [ $debug -eq 1 ] && echo "SPLIT: $key=$value"

    # Quote value in case not quoted
    [[ ! $value =~ ^\" ]] && value="\"$value\""

    # Environment variable format
    # echo "$key=$value" >> ${CICD_FILES_DIR}/${baseNameFile}.env

    # Environment groovy format
    echo "env.$key=$value" >> ${CICD_FILES_DIR}/${baseNameFile}.groovy

  done
  IFS=$OLDIFS
done
