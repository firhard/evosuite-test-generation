PROJECT_PATH=$1
FLAKY_FILTER=$2
MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# check for multiple modules project
if [ ! -f "$PROJECT_PATH/mvn-compile.log" ]; then
    mvn compile -l mvn-compile.log -Drat.skip=true
fi

MODULE_PATH=$(python3 $SCRIPT_DIR/project_modules.py $PROJECT_PATH)
for value in $MODULE_PATH
do 
    #generate tests
    bash $SCRIPT_DIR/generate_evosuite_tests.sh $value $FLAKY_FILTER
done
