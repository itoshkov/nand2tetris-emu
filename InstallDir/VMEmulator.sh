#!/bin/sh
cd `dirname $0`
java -jar ../VMEmulator/target/VMEmulator-*.jar $@
