import re

import os

import sys
from github import Github
import json
from datetime import timezone
import datetime
import argparse

parser = argparse.ArgumentParser(description="Make a new ripme release on github")
parser.add_argument("-f", "--file", help="Path to the version of ripme to release")
parser.add_argument("-t", "--token", help="Your github personal access token")
parser.add_argument("-d", "--debug", help="Run in debug mode", action="store_true")
args = parser.parse_args()


# Make sure the file the user selected is a jar
def isJar(filename):
    if debug:
        print("Checking if {} is a jar file".format(filename))
    return filename.endswith("jar")


def isValidCommitMessage(message):
    if debug:
        print("Checking if {} matchs pattern ^\d+\.\d+\.\d+:".format(message))
    pattern = re.compile("^\d+\.\d+\.\d+:")
    return re.match(pattern, message)


fileToUpload = args.file
commitMessage = json.loads(open("ripme.json").read()).get("changeList")[0]
debug = args.debug


if not os.path.isfile(fileToUpload):
    print("[!] Error: {} does not exist".format(fileToUpload))
    sys.exit(1)

if not isJar(fileToUpload):
    print("[!] Error: {} is not a jar file!".format(fileToUpload))
    sys.exit(1)

if not isValidCommitMessage(commitMessage):
    print("[!] Error: {} is not a valid commit message as it does not start with a version".format(fileToUpload))
    sys.exit(1)