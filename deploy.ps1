Param (
    [Parameter(Mandatory=$True)]
    [string]$source,
    [Parameter(Mandatory=$True)]
    [string]$dest
)

Copy-Item -Path $source -Destination $dest

$sourceHash = (Get-FileHash $source -algorithm MD5).Hash
$destHash = (Get-FileHash $dest -algorithm MD5).Hash
if ($sourceHash -eq $destHash) {
    Write-Output 'Deployed successfully.'
} else {
    Write-Output 'Hash Mismatch: did you close ripme before deploying?'
}
