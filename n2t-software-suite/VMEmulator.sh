#!/usr/bin/env sh

# $Id: VMEmulator.sh,v 1.1 2014/06/17 21:14:01 marka Exp $
# mark.armbrust@pobox.com

# User's CDPATH can interfere with cd in this script
unset CDPATH
# Get the true name of this script
script="`test -L "$0" && readlink -n "$0" || echo "$0"`"
dir="$PWD"
cd "`dirname "$script"`"
if [ \( $# -gt 1 \) -o \( "$1" = "-h" \) -o \( "$1" = "--help" \) ]
then
	echo "Usage:"
	echo "    `basename "$0"`             Starts the VM Emulator in interactive mode."
	echo "    `basename "$0"` FILE.tst    Starts the VM Emulator and runs the FILE.tst test"
	echo "                              script.  The success/failure message is"
	echo "                              printed to the command console."	
elif [ $# -eq 0 ]
then
	# Run VM emulator in interactive mode
  java -jar bin/VMEmulator.jar
else
	# Convert arg1 to an absolute path and run VM emulator with arg1
	if [ `echo "$1" | sed -e "s/\(.\).*/\1/"` = / ]
	then
		arg1="$1"
	else
		arg1="${dir}/$1"
	fi
#	echo Running "$arg1"
	[ -z "$N2T_VM_USE_BUILTINS" ] && export N2T_VM_USE_BUILTINS="yes"
	java -jar bin/VMEmulator.jar "$arg1"
fi
