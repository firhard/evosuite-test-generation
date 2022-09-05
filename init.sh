
ROOT_PATH=$(dirname "$0")

mkdir dependencies

wget https://github.com/EvoSuite/evosuite/releases/download/v1.2.0/evosuite-1.2.0.jar -P $ROOT_PATH/dependencies
wget https://github.com/EvoSuite/evosuite/releases/download/v1.2.0/evosuite-standalone-runtime-1.2.0.jar -P $ROOT_PATH/dependencies

# wget https://repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.jar -P $ROOT_PATH/dependencies
wget https://repo1.maven.org/maven2/junit/junit/4.13/junit-4.13.jar -P $ROOT_PATH/dependencies
wget https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar -P $ROOT_PATH/dependencies

# wget https://downloads.apache.org/ant/binaries/apache-ant-1.10.12-bin.tar.gz -P $ROOT_PATH/dependencies
# tar zxvf apache-ant-1.10.12-bin.tar.gz -C $ROOT_PATH/dependencies
# mv $ROOT_PATH/dependencies/apache-ant-1.10.12/lib/ant.jar $ROOT_PATH/dependencies
# mv $ROOT_PATH/dependencies/apache-ant-1.10.12/lib/ant-junit.jar $ROOT_PATH/dependencies
# mv $ROOT_PATH/dependencies/apache-ant-1.10.12/lib/ant-launcher.jar $ROOT_PATH/dependencies


