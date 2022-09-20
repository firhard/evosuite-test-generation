#!/bin/bash
#execute tests in module
MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# check for multiple modules project
if [ ! -f "$(pwd)/mvn-compile.log" ]; then
    mvn compile -l mvn-compile.log -Drat.skip=true
fi

MODULE_PATH=$(python3 $MY_PATH/project_modules.py)
# echo $MODULE_PATH
for path in $MODULE_PATH
do 
    cd $path
    mvn dependency:copy-dependencies
    if [ ! -f "$(pwd)/mvn-test.log" ]; then
        mvn test -l mvn-test.log -Drat.skip=true
    fi
    bash $SCRIPT_DIR/run_evosuite_tests.sh
done
