import sys
import re

result = sys.argv[1]
results = result.split("\n")
results = [re.sub('.*Tests run','Tests run', i) for i in results]

resultDictList = []
for rslt in results:
    resultDictList.append(dict(x.split(":") for x in rslt.split(", ")))

for sub in resultDictList:
    for key in sub:
        sub[key] = int(sub[key])

resultDict = {}
for d in resultDictList:
    for k in d.keys():
        resultDict[k] = resultDict.get(k, 0) + d[k]

numTests = resultDict["Tests run"]

if numTests == 0: exit(1)

numNonPassTests = 0
for key in resultDict:
    if key != "Tests run" and key != "Flakes" and key != "Failures":
        numNonPassTests += resultDict[key]

if numNonPassTests >= numTests: exit(1)

# Tests with error will stop other tests when running 'mvn test' (hence exit '1' status)
if resultDict["Errors"] > 0: exit(1)