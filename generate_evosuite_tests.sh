#!/bin/bash
MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

PROJECT_PATH=$1
FLAKY_FILTER=$2
mkdir $PROJECT_PATH/evosuite-tests
# generate tests
if [[ $FLAKY_FILTER == 0 ]] 
then
    java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $PROJECT_PATH/target/classes/ -Dctg_cores=1 -Dctg_memory=1000 -Dctg_bests_folder=evosuite-tests -Dctg_dir=$PROJECT_PATH -continuous EXECUTE -Dctg_time_per_class=1

    testDEPENDENCIES=$(find ./dependencies -type f -name \*.jar | tr '\n' ':')

    #set classpath to run developer-written test
    export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-tests/:./test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes

    TESTS=$(find $PROJECT_PATH/evosuite-tests/ -type f  -name \*.java)
    echo "Compiling EvoSuite tests"
    for x in $TESTS; do
        JAVA_RESPONSE="$(javac $x 2>&1)";
        # remove test class that fail to compile
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

    EVOSUITE_TESTS=$(find $PROJECT_PATH/evosuite-tests/ -type f 2> /dev/null | wc -l)
    if [[ $EVOSUITE_TESTS == 0 ]]
    then 
        exit 1; 
    fi
else
    java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite \
    -target $PROJECT_PATH/target/classes \
    -Dtest_scaffolding=false \
    -Dno_runtime_dependency=true \
    -Djunit_check=false \
    -Dsandbox=false \
    -Dvirtual_fs=false \
    -Dvirtual_net=false \
    -Dreplace_calls=false \
    -Dreplace_system_in=false \
    -Dreplace_gui=false \
    -Dreset_static_fields=false \
    -Dreset_static_field_gets=false \
    -Dreset_static_final_fields=false \ 
    -Dctg_cores=1  \
    -Dctg_memory=1000 \
    -Dctg_bests_folder=evosuite-flaky \
    -Dctg_dir=$PROJECT_PATH \
    -continuous EXECUTE \
    -Dctg_time_per_class=2

    testDEPENDENCIES=$(find ./dependencies -type f -name \*.jar | tr '\n' ':')

    #set classpath to run developer-written test
    export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-flaky/:./test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes

    TESTS=$(find $PROJECT_PATH/evosuite-flaky/ -type f  -name \*.java)
    echo "Compiling EvoSuite tests"
    for x in $TESTS; do
        JAVA_RESPONSE="$(javac $x 2>&1)";
        # remove test class that fail to compile
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

    EVOSUITE_TESTS=$(find $PROJECT_PATH/evosuite-flaky -type f 2> /dev/null | wc -l)
    if [[ $EVOSUITE_TESTS == 0 ]]
    then 
        exit 1; 
    fi
fi

