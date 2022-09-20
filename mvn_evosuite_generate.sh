#!/bin/bash
# mvn evosuite:generate -Drat.skip=true
current_path=$(dirname "$0")
mvn compile -Drat.skip=true

mv $(pwd)/src/test $(pwd)/dev_tests
python3 $current_path/modify_pom_xml.py $(pwd)/pom.xml
mvn evosuite:generate -DtimeInMinutesPerClass=0.5 -DmemoryInMB=2000 -Dcores=4 -Drat.skip=true 
if [[ "$?" -ne 0 ]] ; then
    echo 'Could not perform mvn evosuite:generate, it will generate evosuite tests manually';
    bash $current_path/run_evosuite_tests.sh
else 
    mvn evosuite:export -Drat.skip=true
fi

mvn test -Drat.skip=true
# python3 $current_path/run_test_random_order.py

# rename folder back dwt to test
mv $(pwd)/dev_tests $(pwd)/src/test