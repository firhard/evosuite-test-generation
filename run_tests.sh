SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PROJECT_PATH_SOURCE=$1
PROJECT_PATH=$2
ORDER=$3
TEST_NUMBER=$4
REPORT_PATH=$5
ORDER_MOD=$(expr $ORDER % 2)

cp -Rp $PROJECT_PATH_SOURCE $PROJECT_PATH

if [ $ORDER -le 1 ]; then
    bash $SCRIPT_DIR/run_developer_written_tests.sh $PROJECT_PATH $ORDER_MOD $TEST_NUMBER $REPORT_PATH
elif [ $ORDER -le 3 ]; then
    bash $SCRIPT_DIR/run_evosuite_tests.sh $PROJECT_PATH $ORDER_MOD $TEST_NUMBER $REPORT_PATH
elif [ $ORDER -le 5 ]; then
    bash $SCRIPT_DIR/run_evosuite-flaky.sh $PROJECT_PATH $ORDER_MOD $TEST_NUMBER $REPORT_PATH
fi