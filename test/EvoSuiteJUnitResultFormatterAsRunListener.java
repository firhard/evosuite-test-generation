import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class EvoSuiteJUnitResultFormatterAsRunListener extends RunListener {
    private final EvoSuiteTestRunner evoSuiteTestRunner;
    protected final EvoSuiteXMLFormatter formatter;
    private int problem;
    private long startTime;
    private String displayName;

    EvoSuiteJUnitResultFormatterAsRunListener(EvoSuiteTestRunner evoSuiteTestRunner, EvoSuiteXMLFormatter formatter) {
        this.evoSuiteTestRunner = evoSuiteTestRunner;
        this.formatter = formatter;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        displayName = description.getDisplayName();
        formatter.startTestSuite(new JUnitTest(displayName));
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        JUnitTest suite = new JUnitTest(displayName);
        suite.setCounts(1, problem, 0);
        suite.setRunTime(System.currentTimeMillis() - startTime);
        formatter.endTestSuite(suite);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        formatter.startTest(new EvoSuiteDescriptionAsTest(evoSuiteTestRunner, description));
        problem = 0;
        startTime = System.currentTimeMillis();
    }

    @Override
    public void testFinished(Description description) throws Exception {
        formatter.endTest(new EvoSuiteDescriptionAsTest(evoSuiteTestRunner, description));
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        testAssumptionFailure(failure);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        problem++;
        formatter.addError(new EvoSuiteDescriptionAsTest(evoSuiteTestRunner, failure.getDescription()), failure.getException());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
    }
}
