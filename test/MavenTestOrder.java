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
import java.util.Comparator;
import java.util.Collection;
// import org.junit.runner.manipulation.Filter;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.Request;
import org.junit.runner.notification.RunListener;
import org.junit.runner.manipulation.Ordering;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Orderer;
import org.junit.runner.manipulation.InvalidOrderingException;
import org.junit.runner.manipulation.Orderable;
import org.junit.runner.OrderWith;
// import org.junit.runners.model.TestClass;
// import org.junit.runner.manipulation.Orderable;

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
    private static String log = "";

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
        final List<Class> classes = new ArrayList<>();
        int pass = 0;
        int fail = 0;
        JUnitCore junit = new JUnitCore();

        for (String clazz : classOrder) {
            classes.add(Class.forName(clazz));
        }

        if (testOrder.equals("shuffle")){
            Collections.shuffle(classes);
        }

        junit.addListener(new JUnitResultFormatterAsRunListener(new XMLJUnitResultFormatter()) {
            @Override
            public void testRunStarted(Description description) throws Exception {
                // System.out.println(description.getClassName());
                if (testOrder.equals("shuffle")) formatter.setOutput(new FileOutputStream(new File("/home/firhard/Documents/flakyTestGeneration/datasets/new/jmeter-datadog-backend-listener/test-reports","TEST-" + description.getDisplayName() + "-shuffle-" + System.currentTimeMillis() + ".xml")));
                else formatter.setOutput(new FileOutputStream(new File("/home/firhard/Documents/flakyTestGeneration/datasets/new/jmeter-datadog-backend-listener/test-reports","TEST-" + description.getDisplayName() + "-" + System.currentTimeMillis() + ".xml")));
                
                super.testRunStarted(description);
            }

            @Override
            public void testStarted(Description description) throws Exception {
                // if (testOrder.equals("shuffle")) formatter.setOutput(new FileOutputStream(new File("/home/firhard/Documents/flakyTestGeneration/datasets/new/jmeter-datadog-backend-listener/test-reports","TEST-" + description.getDisplayName() + "-shuffle.xml")));
                // else formatter.setOutput(new FileOutputStream(new File("/home/firhard/Documents/flakyTestGeneration/datasets/new/jmeter-datadog-backend-listener/test-reports","TEST-" + description.getDisplayName() + ".xml")));
                
                super.testStarted(description);
            }
        });

        Result result = junit.run(Request.classes(classes.toArray(new Class[0]))
        .orderWith(new Ordering() {
            public boolean validateOrderingIsCorrect() {
                return false;
            }

            public List<Description> orderItems(Collection<Description> descriptions) {
                List<Description> ordered = new ArrayList<>(descriptions);
                ArrayList<Description> shuffled = new ArrayList<>(descriptions.size());
                ordered.forEach((Description description) -> {
                    Description childDescription = description.childlessCopy();
                    List<Description> childrens = new ArrayList<>(description.getChildren());
                    if (testOrder.equals("shuffle")){
                        Collections.shuffle(childrens);
                    }
                    for(Description children : childrens){
                        childDescription.addChild(children);
                        
                    }
                    shuffled.add(childDescription);
                });
                if (testOrder.equals("shuffle")){
                    Collections.shuffle(shuffled);
                }
                return shuffled;
            }
        }));
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
                if (line.trim().startsWith("[INFO] Running ") && !line.trim().contains("$")) {
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


    // public static class JUnitResultFormatterTest implements JUnitResultFormatter {
    //     @Override
    //     public void startTest(final Test t) {
    //         System.out.println(t);
    //         super.startTest(t);
    //         // testStarts.put(createDescription(t), System.currentTimeMillis());
    //     }
    // }

    public static class JUnitResultFormatterAsRunListener extends RunListener {
        protected final JUnitResultFormatter formatter;
        private ByteArrayOutputStream stdout,stderr;
        private PrintStream oldStdout,oldStderr;
        private int problem;
        private long startTime;
        private String displayName;

        private JUnitResultFormatterAsRunListener(JUnitResultFormatter formatter) {
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
            suite.setCounts(1,problem,0);
            suite.setRunTime(System.currentTimeMillis()-startTime);
            formatter.endTestSuite(suite);
        }

        @Override
        public void testStarted(Description description) throws Exception {
            String testMethod = description.getDisplayName();
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
            return description.getMethodName();
        }

        public String getClassName(){
            return description.getClassName();
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