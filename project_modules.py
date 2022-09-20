import os
import sys

def project_modules(argv):
    projectPath = argv[1]
    lines = open(projectPath + '/mvn-compile.log','r').read().splitlines()
    compiledProjects = [projects for projects in lines if "/target/classes" in projects and "source file" in projects and "Compiling" in projects]
    projectPath = []
    for project in compiledProjects:
        x = project.split(' ')
        projectPath.append(x[len(x)-1])

    projectPath = [paths + '/../..' for paths in projectPath]
    for path in projectPath:
        print(path)

if __name__ == "__main__":
    project_modules(sys.argv)