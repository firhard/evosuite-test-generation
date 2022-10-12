#!/bin/bash
DOCKER_RUN=$1
SCRIPT_FILE=$2
PROJECT_PATH_SOURCE=$3
PROJECT_PATH=$4
ORDER=$5
TEST_NUMBER=$6
REPORT_PATH=$7
SCRIPTS_DIR=$8

for i in {0..9}; 
do 
    status=$DOCKER_RUN $SCRIPT_FILE $PROJECT_PATH_SOURCE $PROJECT_PATH $ORDER $TEST_NUMBER $REPORT_PATH"_${i}";

    if [ $status != 0 ]; then
        exit 1
    fi
done