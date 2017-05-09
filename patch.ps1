Param (
    [Parameter(Mandatory=$True)]
    [string]$message
)

# This script will:
# - read current version
# - increment patch version
# - update version in a few places
# - insert new line in ripme.json with $message

$ripmeJson = (Get-Content "ripme.json") -join "`n" | ConvertFrom-Json
$currentVersion = $ripmeJson.latestVersion

Write-Output (("Current version", $currentVersion) -join ' ')

$versionFields = $currentVersion.split('.')
$patchCurr = [int]($versionFields[2])
$patchNext = $patchCurr + 1
$majorMinor = $versionFields[0..1]
$majorMinorPatch = $majorMinor + $patchNext
$nextVersion = $majorMinorPatch -join '.'

Write-Output (("Updating to", $nextVersion) -join ' ')

$substExpr = "s/${currentVersion}/${nextVersion}/"
sed src/main/java/com/rarchives/ripme/ui/UpdateUtils.java -i -e "${substExpr}"
git grep "DEFAULT_VERSION.*${nextVersion}" src/main/java/com/rarchives/ripme/ui/UpdateUtils.java

$substExpr = "s/\`"latestVersion\`" : \`"${currentVersion}\`"/\`"latestVersion\`" : \`"${nextVersion}\`"/"
sed ripme.json -i -e "${substExpr}"
git grep "latestVersion" ripme.json

$substExpr = "s/<version>${currentVersion}/<version>${nextVersion}/"
sed pom.xml -i -e "${substExpr}"
git grep "<version>${nextVersion}" pom.xml

$commitMessage = "${nextVersion}: ${message}"

$ripmeJsonLines = Get-Content "ripme.json"
$ripmeJsonHead = $ripmeJsonLines[0..2]
$ripmeJsonRest = $ripmeJsonLines[3..$ripmeJsonLines.length]
$changelogLine = "    `"${commitMessage}`","
$updatedLines = $ripmeJsonHead + $changelogLine + $ripmeJsonRest + ""
$outputContent = $updatedLines -join "`n"

$outputPath = (Resolve-Path .\ripme.json).Path
$Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding $False
[System.IO.File]::WriteAllText($outputPath, $outputContent, $Utf8NoBomEncoding)

git add -u
git commit -m $commitMessage
git tag $nextVersion
