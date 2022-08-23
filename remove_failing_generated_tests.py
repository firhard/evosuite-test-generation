import os
from xml.etree import ElementTree as ET
import re
import random

def namespace(element):
    m = re.match(r'\{.*\}', element.tag)
    return m.group(0) if m else ''
reportPath = os.getcwd()
# need to have surefire-reports (by running mvn test, it'll generate those reports)
testClasses = os.popen("find " + reportPath + '/target/surefire-reports/ -name "*.txt"').readlines()
# testClasses = [testClass.strip() for testClass in testClasses]
# testClasses = [testClass.split('/')[len(testClass.split('/'))-1] for testClass in testClasses]
# testClasses = [testClass.replace(".", "/") for testClass in testClasses]
# testClasses = [testClass.replace("/txt", "") for testClass in testClasses]
testClasses = os.popen("find " + reportPath + '/target/surefire-reports/ -name "TEST-*"').readlines()
testClasses = [testClass.strip() for testClass in testClasses]
# print(testClasses)


for testClass in testClasses:
    doc = ET.parse(testClass)
    root = doc.getroot()
    ET.register_namespace("", namespace(root).replace("{", "").replace("}", ""))
    doc = ET.parse(testClass)
    root = doc.getroot()
    testcases = []
    testClassname = ''
    error = []
    for testcase in root.findall(namespace(root)+'testcase'):
        for errortestcase in testcase.findall(namespace(root)+'error'):
            error.append("1")
    if len(error) > 0:
        testClass = testClass.split('/')[len(testClass.split('/'))-1]
        testClass = testClass.replace("TEST-", "")
        testClass = testClass.replace(".", "/")
        testClass = testClass.replace("/xml", "")
        ESTestcmd = 'rm ' + reportPath + '/evosuite-tests/' + testClass + '.java'
        os.system(ESTestcmd)
        ESTestcmd = 'rm ' + reportPath + '/evosuite-tests/' + testClass + '_scaffolding.java'
        os.system(ESTestcmd)