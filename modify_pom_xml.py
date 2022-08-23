from xml.etree import ElementTree as ET
import re
import os
import sys

fn = sys.argv[1]
if os.path.exists(fn):
    print(os.path.basename(fn))
else:
    print(os.path.basename(fn)+' does not exist')
    exit()


path = fn
def namespace(element):
    m = re.match(r'\{.*\}', element.tag)
    return m.group(0) if m else ''

# Assume that we have an existing XML document with one "data" child
# ET.register_namespace("", "http://maven.apache.org/POM/4.0.0")
doc = ET.parse(path)

root = doc.getroot()
ET.register_namespace("", namespace(root).replace("{", "").replace("}", ""))
doc = ET.parse(path)

root = doc.getroot()
targetFolder = ET.Element("targetFolder")
targetFolder.text = "evosuite-tests"

properties = root.findall(namespace(root)+'properties')
if len(properties) > 0:
    for property in root.findall(namespace(root)+'properties'):
        property.append(targetFolder)
else:
    propertiesTag = ET.Element("properties")
    propertiesTag.append(targetFolder)
    root.append(propertiesTag)

evosuiteStandAlone = ET.Element("dependency")
evosuiteSAGroupId = ET.Element("groupId")
evosuiteSAGroupId.text = "org.evosuite"
evosuiteSAArtifactId = ET.Element('artifactId')
evosuiteSAArtifactId.text = "evosuite-standalone-runtime"
evosuiteSAVersion = ET.Element("version")
evosuiteSAVersion.text = "1.0.6"
evosuiteSAScope = ET.Element("scope")
evosuiteSAScope.text = "test"
evosuiteStandAlone.append(evosuiteSAGroupId)
evosuiteStandAlone.append(evosuiteSAArtifactId)
evosuiteStandAlone.append(evosuiteSAVersion)
evosuiteStandAlone.append(evosuiteSAScope)

junitDependency = ET.Element("dependency")
junitDependencyGroupId = ET.Element("groupId")
junitDependencyGroupId.text = "junit"
junitDependencyArtifactId = ET.Element('artifactId')
junitDependencyArtifactId.text = "junit"
junitDependencyVersion = ET.Element("version")
junitDependencyVersion.text = "4.12"
junitDependencyScope = ET.Element("scope")
junitDependencyScope.text = "test"
junitDependency.append(junitDependencyGroupId)
junitDependency.append(junitDependencyArtifactId)
junitDependency.append(junitDependencyVersion)
junitDependency.append(junitDependencyScope)

dependencies = root.findall(namespace(root)+'dependencies')
if len(dependencies) > 0:
    for dependency in root.findall(namespace(root)+'dependencies'):
        dependency.append(evosuiteStandAlone)
        dependency.append(junitDependency)
else:
    dependenciesTag = ET.Element("dependencies")
    dependenciesTag.append(evosuiteStandAlone)
    dependenciesTag.append(junitDependency)
    root.append(dependenciesTag)

evosuitePlugin = ET.Element("plugin")
evosuitePluginGroupId = ET.Element("groupId")
evosuitePluginGroupId.text = "org.evosuite.plugins"
evosuitePluginArtifactId = ET.Element('artifactId')
evosuitePluginArtifactId.text = "evosuite-maven-plugin"
evosuitePluginVersion = ET.Element("version")
evosuitePluginVersion.text = "1.0.6"
evosuitePlugin.append(evosuitePluginGroupId)
evosuitePlugin.append(evosuitePluginArtifactId)
evosuitePlugin.append(evosuitePluginVersion)

mavenPlugin = ET.Element("plugin")
mavenPluginGroupId = ET.Element("groupId")
mavenPluginGroupId.text = "org.apache.maven.plugins"
mavenPluginArtifactId = ET.Element('artifactId')
mavenPluginArtifactId.text = "maven-surefire-plugin"
mavenPluginVersion = ET.Element("version")
mavenPluginVersion.text = "2.17"

mavenPluginConfiguration = ET.Element("configuration")
mavenPluginConfigurationRunOrder = ET.Element("runOrder")
mavenPluginConfigurationRunOrder.text = "random"
mavenPluginConfigurationProperties = ET.Element("properties")
mavenPluginConfigurationPropertiesProperty = ET.Element("property")
mavenPluginConfigurationPropertiesPropertyName = ET.Element("name")
mavenPluginConfigurationPropertiesPropertyName.text = "listener"
mavenPluginConfigurationPropertiesPropertyValue = ET.Element("value")
mavenPluginConfigurationPropertiesPropertyValue.text = "org.evosuite.runtime.InitializingListener"
mavenPluginConfigurationPropertiesProperty.append(mavenPluginConfigurationPropertiesPropertyName)
mavenPluginConfigurationPropertiesProperty.append(mavenPluginConfigurationPropertiesPropertyValue)
mavenPluginConfigurationProperties.append(mavenPluginConfigurationPropertiesProperty)
mavenPluginConfiguration.append(mavenPluginConfigurationRunOrder)
mavenPluginConfiguration.append(mavenPluginConfigurationProperties)

mavenPlugin.append(mavenPluginGroupId)
mavenPlugin.append(mavenPluginArtifactId)
mavenPlugin.append(mavenPluginVersion)
mavenPlugin.append(mavenPluginConfiguration)

codehausPlugin = ET.Element("plugin")
codehausPluginGroupId = ET.Element("groupId")
codehausPluginGroupId.text = "org.codehaus.mojo"
codehausPluginArtifactId = ET.Element('artifactId')
codehausPluginArtifactId.text = "build-helper-maven-plugin"
codehausPluginVersion = ET.Element("version")
codehausPluginVersion.text = "1.8"

codehausPluginExecutions = ET.Element("executions")
codehausPluginExecutionsExecution = ET.Element("execution")
codehausPluginExecutionsExecutionId = ET.Element("id")
codehausPluginExecutionsExecutionId.text = "add-test-source"
codehausPluginExecutionsExecutionPhase = ET.Element("phase")
codehausPluginExecutionsExecutionPhase.text = "generate-test-sources"
codehausPluginExecutionsExecutionGoals = ET.Element("goals")
codehausPluginExecutionsExecutionGoalsGoal = ET.Element("goal")
codehausPluginExecutionsExecutionGoalsGoal.text = "add-test-source"
codehausPluginExecutionsExecutionConfiguration = ET.Element("configuration")
codehausPluginExecutionsExecutionConfigurationSources = ET.Element("sources")
codehausPluginExecutionsExecutionConfigurationSourcesSource = ET.Element("source")
codehausPluginExecutionsExecutionConfigurationSourcesSource.text = "${targetFolder}"

codehausPluginExecutionsExecution.append(codehausPluginExecutionsExecutionId)
codehausPluginExecutionsExecution.append(codehausPluginExecutionsExecutionPhase)
codehausPluginExecutionsExecutionGoals.append(codehausPluginExecutionsExecutionGoalsGoal)
codehausPluginExecutionsExecution.append(codehausPluginExecutionsExecutionGoals)
codehausPluginExecutionsExecutionConfigurationSources.append(codehausPluginExecutionsExecutionConfigurationSourcesSource)
codehausPluginExecutionsExecutionConfiguration.append(codehausPluginExecutionsExecutionConfigurationSources)
codehausPluginExecutionsExecution.append(codehausPluginExecutionsExecutionConfiguration)
codehausPluginExecutions.append(codehausPluginExecutionsExecution)

codehausPlugin.append(codehausPluginGroupId)
codehausPlugin.append(codehausPluginArtifactId)
codehausPlugin.append(codehausPluginVersion)
codehausPlugin.append(codehausPluginExecutions)


builds = root.findall(namespace(root)+'build')
if len(builds) > 0:
    for build in root.findall(namespace(root)+'build'):
        for plugin in build.findall(namespace(root)+'plugins'):
            plugin.append(evosuitePlugin)
            plugin.append(mavenPlugin)
            plugin.append(codehausPlugin)

out = ET.tostring(root)
doc.write(path)