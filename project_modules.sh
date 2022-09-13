MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# check for multiple modules project
mvn compile -l mvn-compile.log -Drat.skip=true
# mvn test -l mvn-test.log -Drat.skip=true

MODULE_PATH=$(python3 $MY_PATH/project_modules.py)
# echo $MODULE_PATH
for value in $MODULE_PATH
do 
    cd $value
    bash $SCRIPT_DIR/generate_evosuite_tests.sh
    bash $SCRIPT_DIR/compile_evosuite_tests.sh
done
