#!/bin/bash
PROJECT_PATH_SOURCE=$1
PROJECT_PATH=$2
ORDER=$3
TEST_NUMBER=$4
REPORT_PATH=$5
SCRIPTS_DIR=$6

ORDER_MOD=$(expr $ORDER % 2)
echo $SCRIPT_DIR
cp -Rp $PROJECT_PATH_SOURCE $PROJECT_PATH
MODULES=$(python3 $SCRIPTS_DIR/project_modules.py $PROJECT_PATH)

for MODULE in $MODULES
do 
    if [ $ORDER -le 1 ]; then
        $SCRIPTS_DIR/run_developer_written_tests.sh $MODULE $ORDER_MOD $TEST_NUMBER $REPORT_PATH $SCRIPTS_DIR
    elif [ $ORDER -le 3 ]; then
        $SCRIPTS_DIR/run_evosuite_tests.sh $MODULE $ORDER_MOD $TEST_NUMBER $REPORT_PATH $SCRIPTS_DIR
    elif [ $ORDER -le 5 ]; then
        $SCRIPTS_DIR/run_evosuite-flaky.sh $MODULE $ORDER_MOD $TEST_NUMBER $REPORT_PATH $SCRIPTS_DIR
    fi
done