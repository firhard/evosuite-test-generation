MY_PATH=$(dirname "$0")
mvn clean
mvn compile -Drat.skip=true

javac $MY_PATH/test/TestRunner.java

mkdir $(pwd)/evosuite-tests

# java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $(pwd)/target/classes -Dctg_cores=6 -Dctg_memory=6000  -Dctg_bests_folder=../evosuite-tests -continuous EXECUTE -Dctg_time_per_class=10

export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/dependencies/evosuite-standalone-runtime-1.2.0.jar:$MY_PATH/dependencies/junit-4.12.jar:$MY_PATH/dependencies/hamcrest-core-1.3.jar:$MY_PATH/test

echo $CLASSPATH

mvn dependency:copy-dependencies

TESTS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.java)

echo "Compiling tests"
for x in $TESTS; do
    JAVA_RESPONSE="$(javac $x 2>&1)";
    if [[ $JAVA_RESPONSE == *"error"* ]]; then
        rm $x
    fi
done

TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*)
tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}
java org.junit.runner.JUnitCore ${tclass};

echo "SHUFFLE ORDER"
TEST_CLASS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\* -printf '%P,')
tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
tclass=${tclass//_scaffolding/}
tclass=${tclass//.class/}
tclass=${tclass//\//.}
java -Dclasses=${tclass} TestRunner;

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
