MY_PATH=$(dirname "$0")
mkdir $(pwd)/evosuite-tests

# generate tests
java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $(pwd)/target/classes -Dctg_cores=3 -Dctg_memory=8000  -Dctg_bests_folder=../evosuite-tests -continuous EXECUTE -Dctg_time_per_class=1