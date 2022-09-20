#!/bin/bash
MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar | tr '\n' ':')

PROJECT_PATH=$1
#set classpath to run developer-written test
export CLASSPATH=$PROJECT_PATH/target/classes:$PROJECT_PATH/evosuite-tests/:$SCRIPT_DIR/test:$testDEPENDENCIES:$PROJECT_PATH/target/test-classes


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