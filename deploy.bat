@echo off
powershell -c ".\deploy.ps1 -source (Join-Path target (Get-Item -Path .\target\* -Filter *.jar)[0].Name) -dest ripme.jar"
