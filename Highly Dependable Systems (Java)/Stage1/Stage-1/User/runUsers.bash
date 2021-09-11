#!/bin/bash
NUsers=$1
NByzantine=$2
MaxDistance=$3
SessionTime=$4
EpochLifeTime=$5

for ((i = 1; i <= NUsers; i++)); do
  CMD="mvn exec:java -DmyUsername=\"User${i}\" -DmyNByzantineUsers=\"${NByzantine}\" -DmyMaxDistance=\"${MaxDistance}\" -DmySessionTime=\"${SessionTime}\" -DmyEpochLifeTime=\"${EpochLifeTime}\""
  gnome-terminal -- bash -c "${CMD}"
done
