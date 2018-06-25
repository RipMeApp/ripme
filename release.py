#!/usr/bin/env python3

import re

import os

import sys
from hashlib import sha256
from github import Github
import json
import argparse

parser = argparse.ArgumentParser(description="Make a new ripme release on github")
parser.add_argument("-f", "--file", help="Path to the version of ripme to release")
parser.add_argument("-t", "--token", help="Your github personal access token")
parser.add_argument("-d", "--debug", help="Run in debug mode", action="store_true")
parser.add_argument("-n", "--non-interactive", help="Do not ask for any input from the user", action="store_true")
parser.add_argument("--test", help="Perform a dry run (Do everything but upload new release)", action="store_true")
parser.add_argument("--skip-hash-check", help="Skip hash check (This should only be used for testing)", action="store_true")
args = parser.parse_args()

try:
    # This binds input to raw_input on python2, we do this because input acts like eval on python2
    input = raw_input
except NameError:
    pass


# Make sure the file the user selected is a jar
def isJar(filename):
    if debug:
        print("Checking if {} is a jar file".format(filename))
    return filename.endswith("jar")


# Returns true if last entry to the "changeList" section of ripme.json is in the format of $number.$number.$number: and
# false if not
def isValidCommitMessage(message):
    if debug:
        print(r"Checking if {} matches pattern ^\d+\.\d+\.\d+:".format(message))
    pattern = re.compile(r"^\d+\.\d+\.\d+:")
    return re.match(pattern, message)


# Checks if the update has the name ripme.jar, if not it renames the file
def checkAndRenameFile(path):
    """Check if path (a string) points to a ripme.jar. Returns the possibly renamed file path"""
    if not path.endswith("ripme.jar"):
        print("Specified file is not named ripme.jar, renaming")
        new_path = os.path.join(os.path.dirname(path), "ripme.jar")
        os.rename(path, new_path) 
        return new_path 
    return path


ripmeJson = json.loads(open("ripme.json").read())
fileToUploadPath = checkAndRenameFile(args.file)
InNoninteractiveMode = args.non_interactive
commitMessage = ripmeJson.get("changeList")[0]
releaseVersion = ripmeJson.get("latestVersion")
debug = args.debug
accessToken = args.token
repoOwner = "ripmeapp"
repoName = "ripme"

if not os.path.isfile(fileToUploadPath):
    print("[!] Error: {} does not exist".format(fileToUploadPath))
    sys.exit(1)

if not isJar(fileToUploadPath):
    print("[!] Error: {} is not a jar file!".format(fileToUploadPath))
    sys.exit(1)

if not isValidCommitMessage(commitMessage):
    print("[!] Error: {} is not a valid commit message as it does not start with a version".format(fileToUploadPath))
    sys.exit(1)


if not args.skip_hash_check:
    if debug:
        print("Reading file {}".format(fileToUploadPath))
    ripmeUpdate = open(fileToUploadPath, mode='rb').read()

    # The actual hash of the file on disk
    actualHash = sha256(ripmeUpdate).hexdigest()

    # The hash that we expect the update to have
    expectedHash = ripmeJson.get("currentHash")

    # Make sure that the hash of the file we're uploading matches the hash in ripme.json. These hashes not matching will
    # cause ripme to refuse to install the update for all users who haven't disabled update hash checking
    if expectedHash != actualHash:
        print("[!] Error: expected hash of file and actual hash differ")
        print("[!] Expected hash is {}".format(expectedHash))
        print("[!] Actual hash is {}".format(actualHash))
        sys.exit(1)
else:
    print("[*] WARNING: SKIPPING HASH CHECK")
# Ask the user to review the information before we precede
# This only runs in we're in interactive mode
if not InNoninteractiveMode:
    print("File path: {}".format(fileToUploadPath))
    print("Release title: {}".format(commitMessage))
    print("Repo: {}/{}".format(repoOwner, repoName))
    input("\nPlease review the information above and ensure it is correct and then press enter")

if not args.test:
    print("Accessing github using token")
    g = Github(accessToken)

    print("Creating release")
    release = g.get_user(repoOwner).get_repo(repoName).create_git_release(releaseVersion, commitMessage, "")

    print("Uploading file")
    release.upload_asset(fileToUploadPath, "ripme.jar")
else:
    print("Not uploading release being script was run with --test flag")
