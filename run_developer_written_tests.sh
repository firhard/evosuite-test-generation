MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

mkdir $PROJECT_PATH/test-reports

PROJECT_PATH=$0
ORDER=$1
TEST_NUMBER=$2

mvnDEPENDENCIES=$(find $PROJECT_PATH/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar | tr '\n' ':')

export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-tests/:$SCRIPT_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES
javac $SCRIPT_DIR/test/EvoSuiteTestRunner.java
javac $SCRIPT_DIR/test/MavenTestRunner.java

#run EvoSuite Tests
testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar | tr '\n' ':')
#update classpath again to run EvoSuite tests
export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-tests/:$SCRIPT_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes
TEST_CLASS=$(find $PROJECT_PATH/evosuite-tests -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
tclass=${TEST_CLASS//$PROJECT_PATH\/evosuite-tests\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}

echo "Run EvoSuite tests in Deterministic Order"
java -Dclasses=${tclass} -Dorder=$ORDER -DreportPath=$PROJECT_PATH/test-reports -DtestReport=$TEST_NUMBER EvoSuiteTestRunner &> /dev/null