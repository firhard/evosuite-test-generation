MY_PATH=$(dirname "$0")
mkdir $(pwd)/evosuite-tests

PROJECT_PATH=$1
FLAKY_FILTER=$2

# generate tests
if [[ $FLAKY_FILTER == 0]] then
    java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $PROJECT_PATH/target/classes -Dctg_cores=1 -Dctg_memory=1000  -Dctg_bests_folder=../evosuite-tests -continuous EXECUTE -Dctg_time_per_class=2
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
    -Dctg_bests_folder=../evosuite-flaky \
    -continuous EXECUTE \
    -Dctg_time_per_class=2
fi
