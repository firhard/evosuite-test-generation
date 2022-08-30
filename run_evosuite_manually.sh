MY_PATH=$(dirname "$0")
mvn clean
mvn compile -Drat.skip=true


mkdir $(pwd)/evosuite-tests

# java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $(pwd)/target/classes -Dctg_cores=2 -Dctg_memory=1000  -Dctg_bests_folder=../evosuite-tests -continuous EXECUTE -Dctg_time_per_class=1

export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/dependencies/evosuite-standalone-runtime-1.2.0.jar:$MY_PATH/dependencies/junit-4.12.jar:$MY_PATH/dependencies/hamcrest-core-1.3.jar:$MY_PATH/test:$(pwd)/target/test-classes

echo $CLASSPATH

javac $MY_PATH/test/ShuffleTestRunner.java
javac $MY_PATH/test/MavenTestOrder.java

mvn dependency:copy-dependencies

TESTS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.java)

echo "Compiling tests"
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

mvn test -l mvn-test.log

java -DmvnLogPath=$(pwd)/mvn-test.log MavenTestOrder;
# TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*)
# TEST_CLASS=$(find $(pwd)/target/test-classes -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*)
# # tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\/\//}
# tclass=${TEST_CLASS//$(pwd)\/target\/test-classes\//}
# tclass=${tclass//_scaffolding/}
# tclass=${tclass//.class/}
# tclass=${tclass//\//.}
# echo $tclass
# java org.junit.runner.JUnitCore ${tclass};


TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*)
# TEST_CLASS=$(find $(pwd)/target/test-classes -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*)
tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
# tclass=${TEST_CLASS//$(pwd)\/target\/test-classes\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}
echo $tclass
java org.junit.runner.JUnitCore ${tclass};

echo "SHUFFLE ORDER"
TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}
# for j in {1..10}; do 
    java -Dclasses=${tclass} ShuffleTestRunner;
# done

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
