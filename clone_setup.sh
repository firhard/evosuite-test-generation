#!/bin/bash
set -e

# -- HELPER FUNCTIONS
function debug_echo {
  [[ "${DEBUG}" = 1 ]] && echo "$@"
}

# -- CONSTANTS
DEBUG=1
RESULT_DIR="/results"

# -- PARSE ARGS
PROJECT_URL=$1
PROJECT_HASH=$2
REPOSITORY_DIR=$3
SCRIPTS_DIR=$4
PROJECT_NAME=$(echo $PROJECT_URL | sed -e 's/.*\///g')

# -- DEBUG OUTPUT
echo "-- $0"
debug_echo "    Project name:         $PROJECT_NAME"
debug_echo "    Project url:          $PROJECT_URL"
debug_echo "    Project hash:         $PROJECT_HASH"

echo "${PROJECT_URL##/*/}"

CWD=$(pwd)
# mkdir $CWD/projects
# debug_echo " CWD = ${CWD}"

# -- CLONE / COPY REPO
debug_echo "Clone Repository into ${REPOSITORY_DIR}"
if [[ $PROJECT_URL == http* ]]
then
    git clone "${PROJECT_URL}" "${REPOSITORY_DIR}"
    if [[ -n "$PROJECT_HASH" ]]; then
        cd "${REPOSITORY_DIR}" || exit 1
        git reset --hard "${PROJECT_HASH}" || exit 1
        cd "${CWD}" || exit 1
    fi
    REPO_HASH=$(git --git-dir="${REPOSITORY_DIR}/.git" rev-parse HEAD)
else
    cp -r "${PROJECT_URL}" "${REPOSITORY_DIR}"
fi

cd "${REPOSITORY_DIR}"

mvn compile -l mvn-compile.log -Drat.skip=true
if grep "BUILD FAILURE" $(pwd)/mvn-compile.log; then
    echo "Build failure"
    exit 1
fi

mvn dependency:copy-dependencies -l mvn-dependencies.log
# exit on JUnit3 as tests cannot be shuffled
if grep "junit-3" $(pwd)/mvn-dependencies.log; then
    echo "JUnit 3 will not be included in this experiment (couldn't be shuffled)"
    exit 1
fi

mvn test -l mvn-test.log -Drat.skip=true -Dmaven.test.failure.ignore
RESULT=$(grep "Tests run:" $(pwd)/mvn-test.log | grep -v "Time elapsed:")

if  grep "Tests run:" $(pwd)/mvn-test.log | grep -v "Time elapsed:";
then
    python3.9 $SCRIPTS_DIR/mvnTestResult.py "${RESULT}"
else
    echo "No tests could be found"
    exit 1
fi