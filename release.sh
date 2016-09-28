#!/bin/bash

#set -x

DIST_NAME=n2t-software-suite
RELEASE_DIR=Release

cd $(dirname $0)

rm -rf $RELEASE_DIR

mvn clean package

DIR=$(mktemp -d)
INST_DIR="${DIR}/${DIST_NAME}/bin"

git archive --format=tar HEAD ${DIST_NAME} | tar -x -C ${DIR}

cp HardwareSimulator/target/HardwareSimulator-*.jar $INST_DIR/HardwareSimulator.jar
cp CPUEmulator/target/CPUEmulator-*.jar $INST_DIR/CPUEmulator.jar
cp VMEmulator/target/VMEmulator-*.jar $INST_DIR/VMEmulator.jar

pushd $DIR
zip -r ${DIST_NAME}.zip ${DIST_NAME}
popd
mkdir $RELEASE_DIR
mv $DIR/${DIST_NAME}.zip $RELEASE_DIR
rm -rf $DIR
