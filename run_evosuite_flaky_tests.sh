#!/bin/bash
MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

PROJECT_PATH=$1
ORDER=$2
TEST_NUMBER=$3
REPORT_PATH=$4

mvnDEPENDENCIES=$(find $PROJECT_PATH/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find ./dependencies -type f -name \*.jar | tr '\n' ':')

export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-flaky/:./test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES
javac ./test/EvoSuiteTestRunner.java
javac ./test/MavenTestRunner.java

#run EvoSuite Tests
testDEPENDENCIES=$(find ./dependencies -type f -name \*.jar | tr '\n' ':')
#update classpath again to run EvoSuite tests
export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-flaky/:./test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes
TEST_CLASS=$(find $PROJECT_PATH/evosuite-flaky -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
tclass=${TEST_CLASS//$PROJECT_PATH\/evosuite-flaky\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}

java -Dclasses=${tclass} -Dorder=$ORDER -DreportPath=$REPORT_PATH -DtestReport=$TEST_NUMBER EvoSuiteTestRunner &> /dev/null