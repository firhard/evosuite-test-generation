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
    public static final Path MVN_TEST_LOG = Paths.get("mvn-test.log");
    private final Path mvnTestLog;
    private final Path sureFireDirectory;

    protected void run() throws Exception {
        final List<String> classOrder = getClassOrder(mvnTestLog.toFile());
        final List<Path> allResultsFolders = Files.walk(sureFireDirectory)
                .filter(path -> path.toString().contains("TEST-"))
                .collect(Collectors.toList());
        for (final Path p : allResultsFolders) {
            File f = p.toFile();
            long time = f.lastModified();
            System.out.println(p + " " + parseXML(f));
        }
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
            new MavenTestOrder(mvnLogPath, surefirePath).run();

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(1);
    }

    private MavenTestOrder(String mvnLogPath, String surefirePath) {
        this.mvnTestLog = Paths.get(mvnLogPath);
        this.sureFireDirectory = Paths.get(surefirePath);
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