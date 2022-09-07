MY_PATH=$(dirname "$0")
mvn compile -Drat.skip=true
mvn dependency:copy-dependencies
mvn test -l mvn-test.log -Drat.skip=true

mkdir $(pwd)/test-reports

mvnDEPENDENCIES=$(find $(pwd)/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find $MY_PATH/dependencies -type f -name \*.jar | tr '\n' ':')
# mvnDEPENDENCIES=$(<$(pwd)/cp.txt)
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes:$mvnDEPENDENCIES
javac $MY_PATH/test/EvoSuiteTestRunner.java
javac $MY_PATH/test/MavenTestRunner.java

#Run Developer Tests
testDEPENDENCIES=$(find $MY_PATH/dependencies -type f -name \*.jar  -not -name \*evosuite\* | tr '\n' ':')
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes:$mvnDEPENDENCIES

echo "Run developer-written test in Deterministic order"
java -DsurefirePath=$(pwd)/target/surefire-reports -DmvnLogPath=$(pwd)/mvn-test.log -DreportPath=$(pwd)/test-reports -DtestOrder=OD -Ddependencies=$mvnDEPENDENCIES MavenTestRunner;

echo "Run developer-written test in shuffle order"
java -DsurefirePath=$(pwd)/target/surefire-reports -DmvnLogPath=$(pwd)/mvn-test.log -DreportPath=$(pwd)/test-reports -DtestOrder=shuffle -Ddependencies=$mvnDEPENDENCIES MavenTestRunner;

#run EvoSuite Tests
testDEPENDENCIES=$(find $MY_PATH/dependencies -type f -name \*.jar | tr '\n' ':')
#update classpath again to run EvoSuite tests
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes

TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}

echo "Run EvoSuite tests in Deterministic Order"
java -Dclasses=${tclass} -Dorder=OD -DreportPath=$(pwd)/test-reports EvoSuiteTestRunner &> /dev/null

echo "Run EvoSuite tests in Shuffled order"
java -Dclasses=${tclass} -Dorder=shuffle -DreportPath=$(pwd)/test-reports EvoSuiteTestRunner &> /dev/null