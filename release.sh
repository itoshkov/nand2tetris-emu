#!/bin/bash

#set -x

RELEASE_DIR=Release

cd $(dirname $0)

rm -rf $RELEASE_DIR

mvn clean package

DIR=$(mktemp -d)
INST_DIR="${DIR}/nand2tetris/tools/bin"

git archive --format=tar HEAD nand2tetris | tar -x -C ${DIR}

cp HardwareSimulator/target/HardwareSimulator-*.jar $INST_DIR/HardwareSimulator.jar
cp CPUEmulator/target/CPUEmulator-*.jar $INST_DIR/CPUEmulator.jar
cp VMEmulator/target/VMEmulator-*.jar $INST_DIR/VMEmulator.jar

pushd $DIR
zip -r nand2tetris.zip nand2tetris/
popd
mkdir $RELEASE_DIR
mv $DIR/nand2tetris.zip $RELEASE_DIR
rm -rf $DIR
