#!/bin/bash
cd $(dirname $0)
if [[ $# -eq 0 ]]; then
    java -jar ../VMEmulator/target/VMEmulator-*.jar
elif [[ $# -eq 1 ]]; then
    [ -z "$N2T_VM_USE_BUILTINS" ] && export N2T_VM_USE_BUILTINS="yes"
    java -jar ../VMEmulator/target/VMEmulator-*.jar "$1"
else
    echo "The program expects 0 or 1 arguments!"
    exit 1
fi
