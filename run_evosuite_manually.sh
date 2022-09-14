MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

mkdir $(pwd)/test-reports

mvnDEPENDENCIES=$(find $(pwd)/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar | tr '\n' ':')

export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$SCRIPT_DIR/test:$testDEPENDENCIES:$(pwd)/target/test-classes:$mvnDEPENDENCIES
javac $SCRIPT_DIR/test/EvoSuiteTestRunner.java
javac $SCRIPT_DIR/test/MavenTestRunner.java

#Run Developer Tests
testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar  -not -name \*evosuite\* -not -name \*hamcrest\* -not -name \*tools.jar\* | tr '\n' ':')
export CLASSPATH=$(pwd)/target/classes:$SCRIPT_DIR/test:$testDEPENDENCIES:$(pwd)/target/test-classes:$mvnDEPENDENCIES

echo "Run developer-written test in Deterministic order"
java -DsurefirePath=$(pwd)/target/surefire-reports -DmvnLogPath=$(pwd)/mvn-test.log -DreportPath=$(pwd)/test-reports -DtestOrder=OD -Ddependencies=$mvnDEPENDENCIES MavenTestRunner
mv $(pwd)/test-reports/TEST-junit-jupiter.xml $(pwd)/test-reports/TEST-class-$(date +%s).xml &> /dev/null
mv $(pwd)/test-reports/TEST-junit-vintage.xml $(pwd)/test-reports/TEST-class-vintage-$(date +%s).xml &> /dev/null

echo "Run developer-written test in shuffle order"
java -DsurefirePath=$(pwd)/target/surefire-reports -DmvnLogPath=$(pwd)/mvn-test.log -DreportPath=$(pwd)/test-reports -DtestOrder=shuffle -Ddependencies=$mvnDEPENDENCIES MavenTestRunner
mv $(pwd)/test-reports/TEST-junit-jupiter.xml $(pwd)/test-reports/TEST-class-shuffle-$(date +%s).xml &> /dev/null
mv $(pwd)/test-reports/TEST-junit-vintage.xml $(pwd)/test-reports/TEST-class-shuffle-vintage-$(date +%s).xml &> /dev/null

#run EvoSuite Tests
testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar | tr '\n' ':')
#update classpath again to run EvoSuite tests
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$SCRIPT_DIR/test:$testDEPENDENCIES:$(pwd)/target/test-classes
TEST_CLASS=$(find $(pwd)/evosuite-tests -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}

echo "Run EvoSuite tests in Deterministic Order"
java -Dclasses=${tclass} -Dorder=OD -DreportPath=$(pwd)/test-reports EvoSuiteTestRunner

echo "Run EvoSuite tests in Shuffled order"
java -Dclasses=${tclass} -Dorder=shuffle -DreportPath=$(pwd)/test-reports EvoSuiteTestRunner