import json
import subprocess

# This script will:
# - read current version
# - increment patch version
# - update version in a few places
# - insert new line in ripme.json with message

message = input('message: ')

with open('ripme.json') as dataFile:
    ripmeJson = json.load(dataFile)
currentVersion = ripmeJson["latestVersion"]

print ('Current version ' + currentVersion)

versionFields = currentVersion.split('.')
patchCur = int(versionFields[2])
patchNext = patchCur + 1
majorMinor = versionFields[:2]
majorMinor.append(str(patchNext))
nextVersion = '.'.join(majorMinor)

print ('Updating to ' + nextVersion)

substrExpr = 's/' + currentVersion + '/' + nextVersion + '/'
subprocess.call(['sed', '-i', '-e', substrExpr, 'src/main/java/com/rarchives/ripme/ui/UpdateUtils.java'])
subprocess.call(['git', 'grep', 'DEFAULT_VERSION.*' + nextVersion,
                 'src/main/java/com/rarchives/ripme/ui/UpdateUtils.java'])

substrExpr = 's/\\\"latestVersion\\\": \\\"' + currentVersion + '\\\"/\\\"latestVersion\\\": \\\"' +\
             nextVersion + '\\\"/'
subprocess.call(['sed', '-i', '-e', substrExpr, 'ripme.json'])
subprocess.call(['git', 'grep', 'latestVersion', 'ripme.json'])

substrExpr = 's/<version>' + currentVersion + '/<version>' + nextVersion + '/'
subprocess.call(['sed', '-i', '-e', substrExpr, 'pom.xml'])
subprocess.call(['git', 'grep', '<version>' + nextVersion + '</version>', 'pom.xml'])

commitMessage = nextVersion + ': ' + message
changeLogLine = '        \"' + commitMessage + '\",\n'

dataFile = open("ripme.json", "r")
ripmeJsonLines = dataFile.readlines()
ripmeJsonLines.insert(3, changeLogLine)
outputContent = ''.join(ripmeJsonLines)
dataFile.close()

dataFile = open("ripme.json", "w")
dataFile.write(outputContent)
dataFile.close()

subprocess.call(['git', 'add', '-u'])
subprocess.call(['git', 'commit', '-m', commitMessage])
subprocess.call(['git', 'tag', nextVersion])
