import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitVersionHelper;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Ordering;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class EvoSuiteTestRunner {
    private final List<String> testClassesList;
    private final String testOrder;
    private final String reportPath;
    String className;

    private EvoSuiteTestRunner(List<String> testClassesList, String testOrder, String reportPath) {
        this.testClassesList = testClassesList;
        this.testOrder = testOrder;
        this.reportPath = reportPath;
    }

    public static void main(String[] args) throws Exception {
        String testClasses = System.getProperty("classes");
        String testOrder = System.getProperty("order");
        String reportPath = System.getProperty("reportPath");
        List<String> testClassesList = Arrays.asList(testClasses.split(","));
        new EvoSuiteTestRunner(testClassesList, testOrder, reportPath).run();

    }

    protected void run() throws Exception {
        List<Class> classes = new ArrayList<>();
        if (testOrder.equals("shuffle")) {
            Collections.shuffle(testClassesList); // shuffle test classes
        }

        for (String testClass : testClassesList) {
            classes.add(Class.forName(testClass));
        }

        JUnitCore junit = new JUnitCore();

        junit.addListener(new EvoSuiteJUnitResultFormatterAsRunListener(this, new EvoSuiteXMLFormatter(this)) {
            @Override
            public void testRunStarted(Description description) throws Exception {
                if (testOrder.equals("shuffle"))
                    formatter.setOutput(new FileOutputStream(new File(reportPath, "TEST-" + description.getDisplayName()
                            + "-shuffle-evosuite-" + System.currentTimeMillis() + ".xml")));
                else
                    formatter.setOutput(new FileOutputStream(new File(reportPath, "TEST-" + description.getDisplayName()
                            + "-evosuite-" + System.currentTimeMillis() + ".xml")));
                super.testRunStarted(description);
            }
        });

        Result result = junit.run(Request.classes(classes.toArray(new Class<?>[classes.size()]))
        .orderWith(new Ordering() {
            public List<Description> orderItems(Collection<Description> descriptions) {
                List<Description> ordered = new ArrayList<>(descriptions);
                if (testOrder.equals("shuffle")) {
                    Collections.shuffle(ordered);
                }
                return ordered;
            }
        }));
        System.exit(0);
    }
}