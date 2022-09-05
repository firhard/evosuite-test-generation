MY_PATH=$(dirname "$0")
mvn clean
mvn compile -Drat.skip=true

# remove test-reports
# rm -r $(pwd)/test-reports
mkdir $(pwd)/evosuite-tests
mkdir $(pwd)/test-reports

# java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $(pwd)/target/classes -Dctg_cores=2 -Dctg_memory=1000  -Dctg_bests_folder=../evosuite-tests -continuous EXECUTE -Dctg_time_per_class=1
mvn dependency:copy-dependencies

mvnDEPENDENCIES=$(find $(pwd)/target/dependency -type f  | tr '\n' ':')
testDEPENDENCIES=$(find $MY_PATH/dependencies -type f -name \*.jar | tr '\n' ':')

export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes:$mvnDEPENDENCIES

javac $MY_PATH/test/EvoSuiteTestRunner.java
javac $MY_PATH/test/MavenTestRunner.java


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

mvn test -l mvn-test.log -Drat.skip=true

echo "Run developer-written test in \`mvn test\` order"
java -DmvnLogPath=$(pwd)/mvn-test.log -DreportPath=$(pwd)/test-reports -DtestOrder=OD MavenTestRunner;

echo "Run developer-written test in shuffle order"
java -DmvnLogPath=$(pwd)/mvn-test.log -DreportPath=$(pwd)/test-reports -DtestOrder=shuffle MavenTestRunner;

#update classpath again to run EvoSuite tests
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes

# TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*)
# tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
# tclass=${tclass//_scaffolding/}
# tclass=${tclass//.class/}
# tclass=${tclass//\//.}
# java org.junit.runner.JUnitCore ${tclass};

TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}

echo "Run EvoSuite tests"
java -Dclasses=${tclass} -Dorder=OD -DreportPath=$(pwd)/test-reports EvoSuiteTestRunner;

echo "Run EvoSuite tests in shuffle order"
java -Dclasses=${tclass} -Dorder=shuffle -DreportPath=$(pwd)/test-reports EvoSuiteTestRunner;

# ignore this part for now
# mvn test -Drat.skip=true
# python3 $MY_PATH/remove_failing_generated_tests.py
# mvn test -Drat.skip=true
# TESTS_COMPILED=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class)
# for x in $TESTS_COMPILED; do
#         rm $x
# done
# python3 $MY_PATH/run_test_random_order.py
# for j in {1..1}; do 
    # echo $tclass
# done