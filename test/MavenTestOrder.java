import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

public class MavenTestOrder {
    private final Path mvnTestLog;
    private final Path sureFireDirectory;
    private final String testOrder;

    protected void run() throws Exception {
        final List<String> classOrder = getClassOrder(mvnTestLog.toFile());
        final List<Path> allResultsFolders = Files.walk(sureFireDirectory)
                .filter(path -> path.toString().contains("TEST-"))
                .collect(Collectors.toList());
        final String shuffleOrder = testOrder;
        int pass = 0;
        int fail = 0;
        JUnitCore junit = new JUnitCore();
        
        final RunListener listener = new RunListener();
        junit.addListener(listener);
        // junit.addListener(new JUnitResultFormatterAsRunListener(new XMLJUnitResultFormatter()) {
        //     @Override
        //     public void testStarted(Description description) throws Exception {
        //         formatter.setOutput(new FileOutputStream(new File("/home/firhard/Documents/flakyTestGeneration/datasets/new/jackson-annotations","TEST-"+description.getDisplayName()+".xml")));
        //         super.testStarted(description);
        //     }
        // });
        
        // junit.addListener(new MyJunitListener());
        for (String clazz : classOrder){
            for (final Path p : allResultsFolders) {
                if (p.toString().contains(clazz)){
                    File f = p.toFile();
                    List<String> testMethods = parseXML(f);

                    for (String testMethod : testMethods) {
                        Request request = Request.method(Class.forName(clazz), testMethod);
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
}