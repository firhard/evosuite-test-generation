
ROOT_PATH=$(dirname "$0")

mkdir dependencies

wget https://github.com/EvoSuite/evosuite/releases/download/v1.2.0/evosuite-1.2.0.jar -P $ROOT_PATH/dependencies
wget https://github.com/EvoSuite/evosuite/releases/download/v1.2.0/evosuite-standalone-runtime-1.2.0.jar -P $ROOT_PATH/dependencies

wget https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-1.0.6.jar -P $ROOT_PATH/dependencies
wget https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-standalone-runtime-1.0.6.jar -P $ROOT_PATH/dependencies

wget https://repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.jar -P $ROOT_PATH/dependencies
wget https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar -P $ROOT_PATH/dependencies

