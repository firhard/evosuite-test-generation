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

#Run Developer Tests
testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar  -not -name \*evosuite\* -not -name \*hamcrest\* -not -name \*tools.jar\* | tr '\n' ':')
export CLASSPATH=$PROJECT_PATH/target/classes:$SCRIPT_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes:$mvnDEPENDENCIES

java -DsurefirePath=$PROJECT_PATH/target/surefire-reports -DmvnLogPath=$PROJECT_PATH/mvn-test.log -DreportPath=$PROJECT_PATH/test-reports -DtestOrder=$ORDER -Ddependencies=$mvnDEPENDENCIES -DtestReport=$TEST_NUMBER MavenTestRunner &> /dev/null
mv $PROJECT_PATH/test-reports/TEST-junit-jupiter.xml $PROJECT_PATH/test-reports/TEST-class-$TEST_NUMBER.xml &> /dev/null
mv $PROJECT_PATH/test-reports/TEST-junit-vintage.xml $PROJECT_PATH/test-reports/TEST-class-vintage-$TEST_NUMBER.xml &> /dev/null