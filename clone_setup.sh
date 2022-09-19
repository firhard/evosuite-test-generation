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
PROJECT_NAME=$(echo $PROJECT_URL | sed -e 's/.*\///g')

# -- DEBUG OUTPUT
echo "-- $0"
debug_echo "    Project name:         $PROJECT_NAME"
debug_echo "    Project url:          $PROJECT_URL"
debug_echo "    Project hash:         $PROJECT_HASH"

echo "${PROJECT_URL##/*/}"

CWD=$(pwd)
mkdir $CWD/projects
debug_echo " CWD = ${CWD}"

# -- CLONE / COPY REPO
REPOSITORY_DIR="${CWD}/projects/${PROJECT_NAME}"
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
    exit 1 
fi
mvn dependency:copy-dependencies

mvn test -l mvn-test.log -Drat.skip=true
# add check how many test fails
# TEST_FAILURE=$(grep "Tests run:" $(pwd)/mvn-test.log)

#0 for with flaky tests fiter
bash $CWD/project_modules.sh ${REPOSITORY_DIR} 0

# bash $CWD/project_modules.sh ${REPOSITORY_DIR} 1 #this is without flaky tests filter