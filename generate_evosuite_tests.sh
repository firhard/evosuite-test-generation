#!/bin/bash
MY_PATH=$(dirname "$0")
PROJECT_PATH=$1
FLAKY_FILTER=$2
SCRIPTS_DIR=$3


# generate tests
if [[ $FLAKY_FILTER == 0 ]] 
then
    mkdir $PROJECT_PATH/evosuite-tests
    java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $PROJECT_PATH/target/classes/ -Dctg_cores=1 -Dctg_memory=2000 -Dctg_bests_folder=evosuite-tests -Dctg_dir=$PROJECT_PATH -continuous EXECUTE -Dctg_time_per_class=2

    testDEPENDENCIES=$(find $SCRIPTS_DIR/dependencies -type f -name \*.jar | tr '\n' ':')

    #set classpath to run developer-written test
    export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-tests/:$SCRIPTS_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes

    TESTS_SIZE=$(find $PROJECT_PATH/evosuite-tests/ -type f -name \*.java | wc -l)
    if [[ $TESTS_SIZE == 0 ]]
    then 
        exit 1; 
    fi

    TESTS=$(find $PROJECT_PATH/evosuite-tests/ -type f -name \*.java)
    
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

    EVOSUITE_TESTS=$(find $PROJECT_PATH/evosuite-tests/ -type f -name \*.class 2> /dev/null | wc -l)
    if [[ $EVOSUITE_TESTS == 0 ]]
    then 
        exit 1; 
    fi
else
    mkdir $PROJECT_PATH/evosuite-flaky
    java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $PROJECT_PATH/target/classes -Dtest_scaffolding=false -Dno_runtime_dependency=true -Djunit_check=false -Dsandbox=false -Dvirtual_fs=false -Dvirtual_net=false -Dreplace_calls=false -Dtest_dir=$PROJECT_PATH/evosuite-flaky -Dreplace_system_in=false -Dreplace_gui=false -Dreset_static_fields=false -Dreset_static_field_gets=false -Dreset_static_final_fields=false

    testDEPENDENCIES=$(find $SCRIPTS_DIR/dependencies -type f -name \*.jar | tr '\n' ':')

    #set classpath to run developer-written test
    export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-flaky/:$SCRIPTS_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes

    TESTS_SIZE=$(find $PROJECT_PATH/evosuite-flaky/ -type f -name \*.java | wc -l)
    if [[ $TESTS_SIZE == 0 ]]
    then 
        exit 1; 
    fi

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

    EVOSUITE_TESTS=$(find $PROJECT_PATH/evosuite-flaky -type f -name \*.class 2> /dev/null | wc -l)
    if [[ $EVOSUITE_TESTS == 0 ]]
    then 
        exit 1; 
    fi
fi

