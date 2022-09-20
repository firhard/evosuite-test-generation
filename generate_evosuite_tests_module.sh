#!/bin/bash
PROJECT_PATH=$1
FLAKY_FILTER=$2
MY_PATH=$(dirname "$0")
SCRIPTS_DIR=$3

# check for multiple modules project
if [ ! -f "$PROJECT_PATH/mvn-compile.log" ]; then
    mvn compile -l mvn-compile.log -Drat.skip=true
fi

MODULES=$(python3 ./project_modules.py $PROJECT_PATH)
for MODULE in $MODULES
do 
    #generate tests
    bash $SCRIPTS_DIR/generate_evosuite_tests.sh $MODULE $FLAKY_FILTER $SCRIPTS_DIR
done
