MY_PATH=$(dirname "$0")
mvn compile -Drat.skip=true
mvn dependency:copy-dependencies
mvn test -l mvn-test.log -Drat.skip=true

mkdir $(pwd)/test-reports


mvnDEPENDENCIES=$(find $(pwd)/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find $MY_PATH/dependencies -type f -name \*.jar | tr '\n' ':')
# mvnDEPENDENCIES=$(<$(pwd)/cp.txt)

#set classpath to run developer-written test
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes:$mvnDEPENDENCIES


TESTS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.java)
echo "Compiling EvoSuite tests"
for x in $TESTS; do
    JAVA_RESPONSE="$(javac $x 2>&1)";
    if [[ $JAVA_RESPONSE == *"error"* ]]; then
        rm $x
        if [[ "$x" == *"_scaffolding.java"* ]]; then
            xTest=${x//_scaffolding.java/.java}
            rm $xTest
        else
            xTestScaffolding=${x//.java/_scaffolding.java}
            rm $xTestScaffolding
        fi
    fi
done
echo "EvoSuite Tests Compiled"

javac $MY_PATH/test/EvoSuiteTestRunner.java
javac $MY_PATH/test/MavenTestRunner.java

testDEPENDENCIES=$(find $MY_PATH/dependencies -type f -name \*.jar  -not -name \*evosuite\* | tr '\n' ':')
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes:$mvnDEPENDENCIES

echo "Run developer-written test in \`mvn test\` order"
java -DsurefirePath=$(pwd)/target/surefire-reports -DmvnLogPath=$(pwd)/mvn-test.log -DreportPath=$(pwd)/test-reports -DtestOrder=OD -Ddependencies=$mvnDEPENDENCIES MavenTestRunner;

echo "Run developer-written test in shuffle order"
java -DsurefirePath=$(pwd)/target/surefire-reports -DmvnLogPath=$(pwd)/mvn-test.log -DreportPath=$(pwd)/test-reports -DtestOrder=shuffle -Ddependencies=$mvnDEPENDENCIES MavenTestRunner;

testDEPENDENCIES=$(find $MY_PATH/dependencies -type f -name \*.jar | tr '\n' ':')
#update classpath again to run EvoSuite tests
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes

TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\/\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}

echo "Run EvoSuite tests"
java -Dclasses=${tclass} -Dorder=OD -DreportPath=$(pwd)/test-reports EvoSuiteTestRunner &> /dev/null

echo "Run EvoSuite tests in shuffle order"
java -Dclasses=${tclass} -Dorder=shuffle -DreportPath=$(pwd)/test-reports EvoSuiteTestRunner &> /dev/null