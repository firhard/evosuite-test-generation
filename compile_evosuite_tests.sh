MY_PATH=$(dirname "$0")
testDEPENDENCIES=$(find $MY_PATH/dependencies -type f -name \*.jar | tr '\n' ':')
# mvnDEPENDENCIES=$(<$(pwd)/cp.txt)

#set classpath to run developer-written test
export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$MY_PATH/test:$testDEPENDENCIES:$(pwd)/target/test-classes


TESTS=$(find $(pwd)/evosuite-tests/ -type f  -name \*.java)
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