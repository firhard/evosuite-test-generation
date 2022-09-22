#!/bin/bash
MY_PATH=$(dirname "$0")
SCRIPTS_DIR=$5

PROJECT_PATH=$1
ORDER=$2
TEST_NUMBER=$3
REPORT_PATH=$4

mvnDEPENDENCIES=$(find $PROJECT_PATH/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find $SCRIPTS_DIR/dependencies -type f -name \*.jar | tr '\n' ':')

export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-tests/:$SCRIPTS_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES
javac $SCRIPTS_DIR/test/EvoSuiteTestRunner.java
javac $SCRIPTS_DIR/test/MavenTestRunner.java

#Run Developer Tests
testDEPENDENCIES=$(find $SCRIPTS_DIR/dependencies -type f -name \*.jar  -not -name \*evosuite\* -not -name \*hamcrest\* -not -name \*tools.jar\* | tr '\n' ':')
export CLASSPATH=$PROJECT_PATH/target/classes:$SCRIPTS_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES

java -DsurefirePath=$PROJECT_PATH/target/surefire-reports -DmvnLogPath=$PROJECT_PATH/mvn-test.log -DreportPath=$REPORT_PATH -DtestOrder=$ORDER -Ddependencies=$mvnDEPENDENCIES -DtestReport=$TEST_NUMBER MavenTestRunner &> /dev/null

[[ $ORDER = 0 ]] && extension="classes" || extension="classes-shuffle"

mv $REPORT_PATH/TEST-junit-jupiter.xml $REPORT_PATH/TEST-classes-$extension-$TEST_NUMBER.xml &> /dev/null
mv $REPORT_PATH/TEST-junit-vintage.xml $REPORT_PATH/TEST-classes-vintage-$extension-$TEST_NUMBER.xml &> /dev/null

reportFound=$(find $REPORT_PATH -type f -name \*$extension-$TEST_NUMBER.xml | wc -l)
if [ $reportFound == 0 ]; then
    exit 1
fi