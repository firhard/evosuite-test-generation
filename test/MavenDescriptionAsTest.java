import junit.framework.Test;
import junit.framework.TestResult;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.junit.runner.Description;

public class MavenDescriptionAsTest implements Test {
    private final MavenTestRunner mavenTestRunner;
    private final Description description;

    public MavenDescriptionAsTest(MavenTestRunner mavenTestRunner, Description description) {
        this.mavenTestRunner = mavenTestRunner;
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
        mavenTestRunner.className = description.getClassName();
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

        MavenDescriptionAsTest that = (MavenDescriptionAsTest) o;

        if (!description.equals(that.description))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }
}
