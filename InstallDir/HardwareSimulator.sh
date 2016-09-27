#!/bin/sh
cd `dirname $0`
java -jar ../HardwareSimulator/target/HardwareSimulator-*.jar $@
