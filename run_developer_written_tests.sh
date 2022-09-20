#!/bin/bash
MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

PROJECT_PATH=$1
ORDER=$2
TEST_NUMBER=$3
REPORT_PATH=$4

mvnDEPENDENCIES=$(find $PROJECT_PATH/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find ./dependencies -type f -name \*.jar | tr '\n' ':')

export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-tests/:./test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES
javac ./test/EvoSuiteTestRunner.java
javac ./test/MavenTestRunner.java

#Run Developer Tests
testDEPENDENCIES=$(find ./dependencies -type f -name \*.jar  -not -name \*evosuite\* -not -name \*hamcrest\* -not -name \*tools.jar\* | tr '\n' ':')
export CLASSPATH=$PROJECT_PATH/target/classes:./test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES

java -DsurefirePath=$PROJECT_PATH/target/surefire-reports -DmvnLogPath=$PROJECT_PATH/mvn-test.log -DreportPath=$REPORT_PATH -DtestOrder=$ORDER -Ddependencies=$mvnDEPENDENCIES -DtestReport=$TEST_NUMBER MavenTestRunner &> /dev/null
mv $REPORT_PATH/TEST-junit-jupiter.xml $REPORT_PATH/TEST-class-$TEST_NUMBER.xml &> /dev/null
mv $REPORT_PATH/TEST-junit-vintage.xml $REPORT_PATH/TEST-class-vintage-$TEST_NUMBER.xml &> /dev/null