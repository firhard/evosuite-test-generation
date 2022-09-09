import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitVersionHelper;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.manipulation.Ordering;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

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
            if (testOrder.equals("shuffle")) {
                Collections.shuffle(mSelectors);
            }
            // System.out.println(mSelectors);
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
            System.exit(0);
        } else {
            JUnitCore junit = new JUnitCore();

            if (testOrder.equals("shuffle")) {
                Collections.shuffle(classes);
            }

            junit.addListener(new MavenJUnitResultFormatterAsRunListener(this, new MavenXMLFormatter(this)) {
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
            System.exit(0);
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
        Document doc = dBuilder.parse(xmlFile);
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
                if((!testName.contains("[") || !testName.contains("]")) && classname.contains("."))
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

}