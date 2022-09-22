#!/bin/bash
MY_PATH=$(dirname "$0")


PROJECT_PATH=$1
ORDER=$2
TEST_NUMBER=$3
REPORT_PATH=$4
SCRIPTS_DIR=$5

mvnDEPENDENCIES=$(find $PROJECT_PATH/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find $SCRIPTS_DIR/dependencies -type f -name \*.jar | tr '\n' ':')

export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-tests/:$SCRIPTS_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES
javac $SCRIPTS_DIR/test/EvoSuiteTestRunner.java
javac $SCRIPTS_DIR/test/MavenTestRunner.java

#Run Developer Tests
testDEPENDENCIES=$(find $SCRIPTS_DIR/dependencies -type f -name \*.jar  -not -name \*evosuite\* -not -name \*hamcrest\* -not -name \*tools.jar\* | tr '\n' ':')
export CLASSPATH=$PROJECT_PATH/target/classes:$SCRIPTS_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES
java -DsurefirePath=$PROJECT_PATH/target/surefire-reports -DmvnLogPath=$PROJECT_PATH/mvn-test.log -DreportPath=$REPORT_PATH -DtestOrder=$ORDER -Ddependencies=$mvnDEPENDENCIES -DtestReport=$TEST_NUMBER -DprojectPath=$(dirname "$REPORT_PATH") MavenTestRunner

[[ $ORDER = 0 ]] && extension="classes" || extension="classes-shuffle"

mv $(dirname "$REPORT_PATH")/TEST-junit-jupiter.xml "$REPORT_PATH"_jupiter.xml &> /dev/null
mv $(dirname "$REPORT_PATH")/TEST-junit-vintage.xml "$REPORT_PATH"_vintage.xml &> /dev/null
reportFound=$(find $REPORT_PATH.xml | wc -l)
if [ $reportFound == 0 ]; then
    reportFound=$(find $(dirname "$REPORT_PATH") -name \*_jupiter.xml | wc -l)
    if [ $reportFound == 0 ]; then
        exit 1
    fi
fi