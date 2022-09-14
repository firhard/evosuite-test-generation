MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# check for multiple modules project
if [ ! -f "$(pwd)/mvn-compile.log" ]; then
    mvn compile -l mvn-compile.log -Drat.skip=true
fi

MODULE_PATH=$(python3 $MY_PATH/project_modules.py)
for value in $MODULE_PATH
do 
    cd $value

    #generate tests
    bash $SCRIPT_DIR/generate_evosuite_tests.sh

    #compile generated tests
    bash $SCRIPT_DIR/compile_evosuite_tests.sh

    #update classpath
    testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar | tr '\n' ':')
    export CLASSPATH=$(pwd)/target/classes:$(pwd)/evosuite-tests/:$SCRIPT_DIR/test:$testDEPENDENCIES:$(pwd)/target/test-classes
    TEST_CLASS=$(find $(pwd)/evosuite-tests -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
    tclass=${TEST_CLASS//$(pwd)\/evosuite-tests\//}
    tclass=${tclass//_scaffolding/}
    tclass=${tclass//.class/}
    tclass=${tclass//\//.}

    mkdir $(pwd)/first-report
    echo "Run EvoSuite test once"
    java -Dclasses=${tclass} -Dorder=OD -DreportPath=$(pwd)/first-report EvoSuiteTestRunner 
    
    XML_REPORT=$(find $(pwd)/first-report -type f  -name \*.xml)
    # comment tests that are broken when being run once
    perl $SCRIPT_DIR/rm_broken_tests.pl $XML_REPORT $(pwd)/evosuite-tests
    rm -r $(pwd)/first-report

    # compile non-failing tests only
    bash $SCRIPT_DIR/compile_evosuite_tests.sh
done
