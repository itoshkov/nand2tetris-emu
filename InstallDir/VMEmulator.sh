#!/bin/sh
cd `dirname $0`
java -jar ../VMEmulator/target/VMEmulator-2.5-SNAPSHOT.jar $@
