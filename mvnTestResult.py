import sys
import re

result = sys.argv[1]
result = re.sub('.*Tests run','Tests run', result)
resultDict = dict(x.split(":") for x in result.split(", "))

for key in resultDict:
    resultDict[key] = int(resultDict[key])

numTests = resultDict["Tests run"]

if numTests == 0: exit(1)

numNonPassTests = 0
for key in resultDict:
    if key != "Tests run" and key != "Flakes" and key != "Failures":
        numNonPassTests += resultDict[key]

if numNonPassTests >= numTests: exit(1)

# I'm not sure if we're going to add this or not as tests with error will stop other tests
if resultDict["Errors"] > 0: exit(1)