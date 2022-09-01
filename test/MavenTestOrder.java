import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.Request;
import org.junit.runner.notification.RunListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.junit.internal.TextListener;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.runner.Description;
import java.io.FileOutputStream;
import junit.framework.Test;
import junit.framework.TestResult;

public class MavenTestOrder {
    private final Path mvnTestLog;
    private final Path sureFireDirectory;
    private final String testOrder;

    public static void main(final String[] args) {
        try {
            String mvnLogPath = System.getProperty("mvnLogPath");
            String surefirePath = System.getProperty("surefirePath");
            String testOrder = System.getProperty("testOrder");
            new MavenTestOrder(mvnLogPath, surefirePath, testOrder).run();

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
        final String shuffleOrder = testOrder;
        int pass = 0;
        int fail = 0;
        JUnitCore junit = new JUnitCore();

        if (testOrder.equals("shuffle")){
            Collections.shuffle(classOrder);
        }
        
        // final RunListener listener = new RunListener();
        // junit.addListener(new TextListener(System.out));
        // HashMap<String, TestSet> classMethodCounts = new HashMap<>();
        // ConcurrentRunListener reporter =
        //     createInstance( classMethodCounts, reporterManagerFactory, parallelClasses, false );
        // JUnitCoreRunListener runListener = new JUnitCoreRunListener( reporter, classMethodCounts );
        // startCapture( runListener );
        // junit.addListeners();
        junit.addListener(new XMLListener());
        // junit.addListener(new JUnitResultFormatterAsRunListener(new XMLJUnitResultFormatter()) {
        //     @Override
        //     public void testRunStarted(Description description) throws Exception {
        //         formatter.setOutput(new FileOutputStream(new File("/home/firhard/Documents/flakyTestGeneration/datasets/new/jackson-annotations/test-reports","TEST-" + description.getDisplayName() + ".xml")));
        //         super.testRunStarted(description);
        //     }
        // });
        
        // junit.addListener(new MyJunitListener());
        for (String clazz : classOrder){
            for (final Path p : allResultsFolders) {
                if (p.toString().contains(clazz)){
                    File f = p.toFile();
                    List<String> testMethods = parseXML(f);

                    if (testOrder.equals("shuffle")){
                        Collections.shuffle(testMethods);
                    }
                    // String name = ManagementFactory.getRuntimeMXBean().getName();
                    // System.out.println(name.split("@")[0]);
                    for (String testMethod : testMethods) {
                        Request request = Request.method(Class.forName(clazz), testMethod);
                        // Request request = Request.method(Class.forName(clazz));
                        Result result = junit.run(request);
                        
                        if(result.wasSuccessful() == true) {
                            pass++;
                            System.out.print(".");
                        } else {
                            fail++;
                            System.out.print("E");
                        }
                    }
                }
            }
        }

        // System.out.println(listener);
        System.out.println("");
        System.out.println("Pass: " + pass + ", Fail: " + fail);
        System.out.println("");
        
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
                String testName = eElement.getAttribute("name");
                testNames.add(testName);
            }
        }
        return testNames;
    }

    private MavenTestOrder(String mvnLogPath, String surefirePath, String testOrder) {
        this.mvnTestLog = Paths.get(mvnLogPath);
        this.sureFireDirectory = Paths.get(surefirePath);
        this.testOrder = testOrder;
    }

    private List<String> getClassOrder(File f) {
        List<String> classNames = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(f);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().startsWith("[INFO] Running ")) {
                    String className = line.trim().split(" ")[2];
                    classNames.add(className);
                }
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classNames;
    }

    public class XMLListener extends RunListener {
        @Override
        public void testRunStarted(Description description) throws Exception {
            System.out.println("testRunStarted " + description);
            // System.out.println(description.toString());
        }

        @Override
        public void testStarted(Description description) throws Exception {
            System.out.println("testStarted " + description.getDisplayName());
            // System.out.println(description.toString());
        }
    }

    public static class JUnitResultFormatterAsRunListener extends RunListener {
        protected final JUnitResultFormatter formatter;
        private ByteArrayOutputStream stdout,stderr;
        private PrintStream oldStdout,oldStderr;
        private int problem;
        private long startTime;

        private JUnitResultFormatterAsRunListener(JUnitResultFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            System.out.println("Start");
            formatter.startTestSuite(new JUnitTest(description.getDisplayName()));
            formatter.startTest(new DescriptionAsTest(description));
            problem = 0;
            startTime = System.currentTimeMillis();

            this.oldStdout = System.out;
            this.oldStderr = System.err;
            System.setOut(new PrintStream(stdout = new ByteArrayOutputStream()));
            System.setErr(new PrintStream(stderr = new ByteArrayOutputStream()));
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
        }

        @Override
        public void testStarted(Description description) throws Exception {
            
        }

        @Override
        public void testFinished(Description description) throws Exception {
            // System.out.flush();
            // System.err.flush();
            // System.setOut(oldStdout);
            // System.setErr(oldStderr);

            // formatter.setSystemOutput(stdout.toString());
            // formatter.setSystemError(stderr.toString());
            // formatter.endTest(new DescriptionAsTest(description));

            // JUnitTest suite = new JUnitTest(description.getDisplayName());
            // suite.setCounts(1,problem,0);
            // suite.setRunTime(System.currentTimeMillis()-startTime);
            // formatter.endTestSuite(suite);
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

    public static class DescriptionAsTest implements Test {
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
            return description.getDisplayName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DescriptionAsTest that = (DescriptionAsTest) o;

            if (!description.equals(that.description)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return description.hashCode();
        }
    }
}