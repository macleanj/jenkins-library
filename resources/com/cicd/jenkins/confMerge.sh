#!/usr/bin/env bash
# Script to merge central (fileA) with custom (fileB) environment files.

fileA="${1}" # Central
fileB="${2}" # Custom
debug=0


OLDIFS=$IFS
IFS=$'\n'

# Read in values from fileA
declare -A envArray
for mapping in $(cat "${fileA}" | egrep -v "^#|^[[:space:]]|^$"); do
  key=$(echo $mapping | awk -F "=" '{print $1}')
  value=$(echo $mapping | awk -F "=" '{print $2}')
  envArray[$key]=$value
done

[ $debug -eq 1 ] && echo "Initial A"
[ $debug -eq 1 ] && for x in "${!envArray[@]}"; do printf "[%s]=%s\n" "$x" "${envArray[$x]}" ; done

# Check which variables are the same and make the one in FileB prevailing
for A in $(cat "${fileA}" | egrep -v "^#|^[[:space:]]|^$"); do
  match=0
  for B in $(cat "${fileB}" | egrep -v "^#|^[[:space:]]|^$"); do
    keyA=$(echo "$A" | sed -e "s/[ ]*=.*//g");
    keyB=$(echo "$B" | sed -e "s/[ ]*=.*//g");
    valueB=$(echo $B | awk -F "=" '{print $2}')

    [ $debug -eq 1 ] && echo "Match checking A / B: $keyA / $keyB"
    if [ "$keyA" == "$keyB" ]; then
      match=1
      envArray[$keyA]=${valueB}
    [ $debug -eq 1 ] && echo "MATCH!!!!"
    fi
  done;
done;

[ $debug -eq 1 ] && echo "A with B prevailing"
[ $debug -eq 1 ] && for x in "${!envArray[@]}"; do printf "[%s]=%s\n" "$x" "${envArray[$x]}" ; done

# Check which variables are only in FileB and add them
for B in $(cat "${fileB}" | egrep -v "^#|^[[:space:]]|^$"); do
  match=0
  keyB=$(echo "$B" | sed -e "s/[ ]*=.*//g");
  valueB=$(echo $B | awk -F "=" '{print $2}')

  for A in $(cat "${fileA}" | egrep -v "^#|^[[:space:]]|^$"); do
    keyA=$(echo "$A" | sed -e "s/[ ]*=.*//g");

    [ $debug -eq 1 ] && echo "Unique checking A / B: $keyA / $keyB"
    if [ "$keyA" == "$keyB" ]; then
      match=1
    [ $debug -eq 1 ] && echo "MATCH!!!!"
    fi

  done;
  if [[ $match -eq 0 ]]; then
      envArray[$keyB]=${valueB}
  fi
done;
IFS=$OLDIFS

# Output merged files
for x in "${!envArray[@]}"; do printf "%s=%s\n" "$x" "${envArray[$x]}" ; done

