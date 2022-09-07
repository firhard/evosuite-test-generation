MY_PATH=$(dirname "$0")
mvn clean
mvn compile -Drat.skip=true

# remove test-reports
mkdir $(pwd)/evosuite-tests

# generate tests
java -cp $MY_PATH/dependencies/evosuite-1.2.0.jar org.evosuite.EvoSuite -target $(pwd)/target/classes -Dctg_cores=2 -Dctg_memory=1000  -Dctg_bests_folder=../evosuite-tests -continuous EXECUTE -Dctg_time_per_class=1