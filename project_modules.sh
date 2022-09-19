PROJECT_PATH=$1
FLAKY_FILTER=$2
MY_PATH=$(dirname "$0")
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# check for multiple modules project
if [ ! -f "$1/mvn-compile.log" ]; then
    mvn compile -l mvn-compile.log -Drat.skip=true
fi

MODULE_PATH=$(python3 $SCRIPT_DIR/project_modules.py)
for value in $MODULE_PATH
do 
    cd $value

    #generate tests
    bash $SCRIPT_DIR/generate_evosuite_tests.sh $PROJECT_PATH $FLAKY_FILTER

    #compile generated tests
    bash $SCRIPT_DIR/compile_evosuite_tests.sh $PROJECT_PATH

    #update classpath
    # testDEPENDENCIES=$(find $SCRIPT_DIR/dependencies -type f -name \*.jar | tr '\n' ':')
    # export CLASSPATH=$1/target/classes:$1/evosuite-tests/:$SCRIPT_DIR/test:$testDEPENDENCIES:$1/target/test-classes
    # TEST_CLASS=$(find $1/evosuite-tests -type f  -name \*.class -not -name \*$\* -not -name \*_scaffolding\*  | tr '\n' ',')
    # tclass=${TEST_CLASS//$1\/evosuite-tests\//}
    # tclass=${tclass//_scaffolding/}
    # tclass=${tclass//.class/}
    # tclass=${tclass//\//.}

    # mkdir $1/first-report
    # echo "Run EvoSuite test once"
    # java -Dclasses=${tclass} -Dorder=OD -DreportPath=$1/first-report EvoSuiteTestRunner 
    
    # XML_REPORT=$(find $1/first-report -type f  -name \*.xml)
    # # comment out tests that are broken when being run once
    # perl $SCRIPT_DIR/rm_broken_tests.pl $XML_REPORT $1/evosuite-tests
    # rm -r $1/first-report

    # # compile non-failing tests only
    # bash $SCRIPT_DIR/compile_evosuite_tests.sh
done
