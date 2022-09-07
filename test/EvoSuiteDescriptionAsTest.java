import junit.framework.Test;
import junit.framework.TestResult;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.junit.runner.Description;

public class EvoSuiteDescriptionAsTest implements Test {
    private final EvoSuiteTestRunner evoSuiteTestRunner;
    private final Description description;

    public EvoSuiteDescriptionAsTest(EvoSuiteTestRunner evoSuiteTestRunner, Description description) {
        this.evoSuiteTestRunner = evoSuiteTestRunner;
        this.description = description;
    }

    public int countTestCases() {
        return 1;
    }

    public void run(TestResult result) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@link JUnitResultFormatter} determines the test name by reflection.
     */
    public String getName() {
        evoSuiteTestRunner.className = description.getClassName();
        return description.getMethodName();
    }

    public String toString() {
        return description.getClassName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EvoSuiteDescriptionAsTest that = (EvoSuiteDescriptionAsTest) o;

        return description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }
}
