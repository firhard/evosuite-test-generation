import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.*;
import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.FileUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Ordering;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class MavenTestRunner {
    private final Path mvnTestLog;
    private final Path sureFireDirectory;
    private final String testOrder;
    private final String reportPath;
    private final String dependencies;
    String className;

    public static void main(final String[] args) {
        try {
            String mvnLogPath = System.getProperty("mvnLogPath");
            String surefirePath = System.getProperty("surefirePath");
            String reportPath = System.getProperty("reportPath");
            String testOrder = System.getProperty("testOrder");
            String dependencies = System.getProperty("dependencies");
            new MavenTestRunner(mvnLogPath, surefirePath, reportPath, testOrder, dependencies).run();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(1);
    }

    protected void run() throws Exception {
        final List<String> classOrder = getClassOrder(mvnTestLog.toFile());
        final List<Path> allResultsFolders = Files.walk(sureFireDirectory)
                .filter(path -> path.toString().contains("TEST-"))
                .collect(Collectors.toList());
        final List<Class> classes = new ArrayList<>();
        for (String clazz : classOrder) {
            classes.add(Class.forName(clazz));
        }

        if (dependencies.contains("junit-jupiter") || dependencies.contains("junit-jupiter-api")){
            System.out.println("Using JUnit5");
            List<MethodSelector> mSelectors = new ArrayList<>();
            for (String clazz : classOrder) {
                for (final Path p : allResultsFolders) {
                    if (p.toString().contains("TEST-" + clazz + ".xml")) {
                        File f = p.toFile();
                        List<String> testMethods = parseXML(f);
                        for (String testMethod : testMethods) {
                            mSelectors.add(selectMethod(testMethod));
                        }
                    }
                }
            }
            Collections.shuffle(mSelectors);
            PrintWriter out = new PrintWriter(new StringWriter());
            LegacyXmlReportGeneratingListener listener = new LegacyXmlReportGeneratingListener(
                    Paths.get(reportPath),out);
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(mSelectors)
                    .build();
            Launcher launcher = LauncherFactory.create();
            launcher.discover(request);
            launcher.registerTestExecutionListeners(listener);
            launcher.execute(request);
        } else {
            System.out.println("Using JUnit4");
            JUnitCore junit = new JUnitCore();

            if (testOrder.equals("shuffle")) {
                Collections.shuffle(classes);
            }

            junit.addListener(new JUnitResultFormatterAsRunListener(new XMLFormatter()) {
                @Override
                public void testRunStarted(Description description) throws Exception {
                    if (testOrder.equals("shuffle"))
                        formatter.setOutput(new FileOutputStream(new File(
                                reportPath,
                                "TEST-" + description.getDisplayName() + "-shuffle-" + System.currentTimeMillis()
                                        + ".xml")));
                    else
                        formatter.setOutput(new FileOutputStream(new File(
                                reportPath,
                                "TEST-" + description.getDisplayName() + "-" + System.currentTimeMillis() + ".xml")));
                    super.testRunStarted(description);
                }
            });

            junit.run(Request.classes(classes.toArray(new Class[0]))
                    .orderWith(new Ordering() {
                        public List<Description> orderItems(Collection<Description> descriptions) {
                            List<Description> ordered = new ArrayList<>(descriptions);
                            ArrayList<Description> shuffled = new ArrayList<>(descriptions.size());
                            ordered.forEach((Description description) -> {
                                Description childDescription = description.childlessCopy();
                                List<Description> childrens = new ArrayList<>(description.getChildren());
                                if (testOrder.equals("shuffle")) {
                                    Collections.shuffle(childrens);
                                }
                                for (Description children : childrens) {
                                    childDescription.addChild(children);

                                }
                                shuffled.add(childDescription);
                            });
                            if (testOrder.equals("shuffle")) {
                                Collections.shuffle(shuffled);
                            }
                            return shuffled;
                        }
                    }));
        }

    }

    private MavenTestRunner(String mvnLogPath, String surefirePath, String reportPath, String testOrder, String dependencies) {
        this.mvnTestLog = Paths.get(mvnLogPath);
        this.sureFireDirectory = Paths.get(surefirePath);
        this.reportPath = reportPath;
        this.testOrder = testOrder;
        this.dependencies = dependencies;
    }

    private List<String> parseXML(File xmlFile) throws IOException, SAXException, ParserConfigurationException {
        List<String> testNames = new ArrayList<>();
        String className = "";
        double testTime = 0;
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = (Document) dBuilder.parse(xmlFile);
        Element rootElement = doc.getDocumentElement();
        rootElement.normalize();
        className = rootElement.getAttribute("name");
        testTime = Double.parseDouble(rootElement.getAttribute("time"));
        NodeList nList = doc.getElementsByTagName("testcase");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getElementsByTagName("skipped").getLength() != 0) {
                    continue;
                }
                String classname = eElement.getAttribute("classname");
                String testName = eElement.getAttribute("name");
                if(!testName.contains("[") || !testName.contains("]"))
                    testNames.add(classname + "#" + testName + "()");
            }
        }
        return testNames;
    }
    private List<String> getClassOrder(File f) {
        List<String> classNames = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(f);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().contains("Running ")) {
                    String[] lineWithRunning = line.trim().split(" ");
                    String className1 = lineWithRunning[lineWithRunning.length - 1];
                    classNames.add(className1);
                }
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classNames;
    }

    public class JUnitResultFormatterAsRunListener extends RunListener {
        protected final XMLFormatter formatter;
        private int problem;
        private long startTime;
        private String displayName;

        private JUnitResultFormatterAsRunListener(XMLFormatter formatter) {
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
            formatter.startTest(new DescriptionAsTest(description));
            problem = 0;
            startTime = System.currentTimeMillis();
        }

        @Override
        public void testFinished(Description description) throws Exception {
            formatter.endTest(new DescriptionAsTest(description));
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            testAssumptionFailure(failure);
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            problem++;
            formatter.addError(new DescriptionAsTest(failure.getDescription()), failure.getException());
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            super.testIgnored(description);
        }
    }

    public class DescriptionAsTest implements Test {
        private final Description description;

        public DescriptionAsTest(Description description) {
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
            className = description.getClassName();
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

            DescriptionAsTest that = (DescriptionAsTest) o;

            if (!description.equals(that.description))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return description.hashCode();
        }
    }

    public static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (final Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    public static String createDescription(final Test test) throws BuildException {
        return JUnitVersionHelper.getTestCaseName(test) + "("
                + JUnitVersionHelper.getTestCaseClassName(test) + ")";
    }

    public class XMLFormatter implements JUnitResultFormatter, XMLConstants, IgnoredTestListener {
        private static final double ONE_SECOND = 1000.0;
        private static final String UNKNOWN = "unknown";
        private Document doc;
        private Element rootElement;
        private final Hashtable<String, Element> testElements = new Hashtable<>();
        private final Map<Test, Test> failedTests = new Hashtable<>();
        private final Map<String, Test> skippedTests = new Hashtable<>();
        private final Map<String, Test> ignoredTests = new Hashtable<>();
        private final Map<String, Long> testStarts = new Hashtable<>();
        private OutputStream out;

        @Override
        public void setOutput(final OutputStream out) {
            this.out = out;
        }

        @Override
        public void setSystemOutput(final String out) {
            formatOutput(SYSTEM_OUT, out);
        }

        @Override
        public void setSystemError(final String out) {
            formatOutput(SYSTEM_ERR, out);
        }

        @Override
        public void startTestSuite(final JUnitTest suite) {
            doc = getDocumentBuilder().newDocument();
            rootElement = doc.createElement(TESTSUITE);
            final String n = suite.getName();
            rootElement.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);

            final String timestamp = DateUtils.format(new Date(),
                    DateUtils.ISO8601_DATETIME_PATTERN);
            rootElement.setAttribute(TIMESTAMP, timestamp);
            rootElement.setAttribute(HOSTNAME, getHostname());

            final Element propsElement = doc.createElement(PROPERTIES);
            rootElement.appendChild(propsElement);
            final Properties props = suite.getProperties();
            if (props != null) {
                for (String name : props.stringPropertyNames()) {
                    final Element propElement = doc.createElement(PROPERTY);
                    propElement.setAttribute(ATTR_NAME, name);
                    propElement.setAttribute(ATTR_VALUE, props.getProperty(name));
                    propsElement.appendChild(propElement);
                }
            }
        }

        private String getHostname() {
            String hostname = "localhost";
            try {
                final InetAddress localHost = InetAddress.getLocalHost();
                if (localHost != null) {
                    hostname = localHost.getHostName();
                }
            } catch (final UnknownHostException e) {
            }
            return hostname;
        }

        @Override
        public void endTestSuite(final JUnitTest suite) throws BuildException {
            rootElement.setAttribute(ATTR_TESTS, Long.toString(suite.runCount()));
            rootElement.setAttribute(ATTR_FAILURES, Long.toString(suite.failureCount()));
            rootElement.setAttribute(ATTR_ERRORS, Long.toString(suite.errorCount()));
            rootElement.setAttribute(ATTR_SKIPPED, Long.toString(suite.skipCount()));
            rootElement.setAttribute(ATTR_TIME, Double.toString(suite.getRunTime() / ONE_SECOND));
            if (out != null) {
                Writer wri = null;
                try {
                    wri = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                    new DOMElementWriter().write(rootElement, wri, 0, "  ");
                } catch (final IOException exc) {
                    throw new BuildException("Unable to write log file", exc);
                } finally {
                    if (wri != null) {
                        try {
                            wri.flush();
                        } catch (final IOException ex) {
                            // ignore
                        }
                    }
                    if (out != System.out && out != System.err)
                        FileUtils.close(wri);
                }
            }
        }

        @Override
        public void startTest(final Test t) {
            testStarts.put(createDescription(t), System.currentTimeMillis());
        }

        @Override
        public void endTest(final Test test) {
            final String testDescription = createDescription(test);
            if (!testStarts.containsKey(testDescription))
                startTest(test);
            Element currentTest;
            if (!failedTests.containsKey(test)
                    && !skippedTests.containsKey(testDescription)
                    && !ignoredTests.containsKey(testDescription)) {
                currentTest = doc.createElement(TESTCASE);
                final String n = JUnitVersionHelper.getTestCaseName(test);
                currentTest.setAttribute(ATTR_NAME,
                        n == null ? UNKNOWN : n);
                currentTest.setAttribute(ATTR_CLASSNAME,
                        className);
                rootElement.appendChild(currentTest);
                testElements.put(createDescription(test), currentTest);
            } else {
                currentTest = testElements.get(testDescription);
            }

            final Long l = testStarts.get(createDescription(test));
            currentTest.setAttribute(ATTR_TIME,
                    Double.toString((System.currentTimeMillis() - l) / ONE_SECOND));
        }

        @Override
        public void addFailure(final Test test, final AssertionFailedError t) {
            formatError(FAILURE, test, (Throwable) t);
        }

        @Override
        public void addError(final Test test, final Throwable t) {
            formatError(ERROR, test, t);
        }

        private void formatError(final String type, final Test test, final Throwable t) {
            if (test != null) {
                endTest(test);
                failedTests.put(test, test);
            }

            final Element nested = doc.createElement(type);
            Element currentTest = test == null ? rootElement : testElements.get(createDescription(test));
            currentTest.appendChild(nested);
            final String message = t.getMessage();
            if (message != null && !message.isEmpty())
                nested.setAttribute(ATTR_MESSAGE, t.getMessage());
            nested.setAttribute(ATTR_TYPE, t.getClass().getName());

            final String strace = JUnitTestRunner.getFilteredTrace(t);
            final Text trace = doc.createTextNode(strace);
            nested.appendChild(trace);
        }

        private void formatOutput(final String type, final String output) {
            final Element nested = doc.createElement(type);
            rootElement.appendChild(nested);
            nested.appendChild(doc.createCDATASection(output));
        }

        @Override
        public void testIgnored(final Test test) {
            formatSkip(test, JUnitVersionHelper.getIgnoreMessage(test));
            if (test != null)
                ignoredTests.put(createDescription(test), test);
        }

        public void formatSkip(final Test test, final String message) {
            if (test != null)
                endTest(test);
            final Element nested = doc.createElement("skipped");
            if (message != null)
                nested.setAttribute("message", message);
            Element currentTest = test == null ? rootElement : testElements.get(createDescription(test));
            currentTest.appendChild(nested);
        }

        @Override
        public void testAssumptionFailure(final Test test, final Throwable failure) {
            formatSkip(test, failure.getMessage());
            skippedTests.put(createDescription(test), test);
        }
    }
}