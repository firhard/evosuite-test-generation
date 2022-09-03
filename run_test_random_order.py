import os
from xml.etree import ElementTree as ET
import re
import random

def namespace(element):
    m = re.match(r'\{.*\}', element.tag)
    return m.group(0) if m else ''
reportPath = os.getcwd()
# need to have surefire-reports (by running mvn test, it'll generate those reports)
testClasses = os.popen("find " + reportPath + '/target/surefire-reports/ -name "TEST-*"').readlines()
testClasses = [testClass.strip() for testClass in testClasses]
testClasses = random.sample(testClasses, len(testClasses)) # shuffle test class order

mvnTestArgs = ""

for testClass in testClasses:
    doc = ET.parse(testClass)
    root = doc.getroot()
    ET.register_namespace("", namespace(root).replace("{", "").replace("}", ""))
    doc = ET.parse(testClass)

    root = doc.getroot()
    testcases = []
    testClassname = ''
    for testcase in root.findall(namespace(root)+'testcase'):
        testcases.append(testcase.get('name'))
        testClassname = testcase.get('classname')

    testcases = random.sample(testcases, len(testcases)) # shuffle test methods order
    testcases = '+'.join(testcases)
    mvnTestArgs = mvnTestArgs + testClassname + '#' + testcases
    if testClass != testClasses[len(testClasses)-1]:
        mvnTestArgs = mvnTestArgs + ','
    else:
        mvnTestArgs = mvnTestArgs + '"'

print(mvnTestArgs)
ESTestcmd = 'mvn test -Dtest="' + mvnTestArgs + " -Drat.skip=true"
os.system(ESTestcmd)