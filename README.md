# RipMe

[![Build Status](https://travis-ci.org/4pr0n/ripme.svg?branch=master)](https://travis-ci.org/4pr0n/ripme)
[![Join the chat at https://gitter.im/RipMeApp/Lobby](https://badges.gitter.im/RipMeApp/Lobby.svg)](https://gitter.im/RipMeApp/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Album ripper for various websites. Runs on your computer. Requires Java 8.

![Screenshot](http://i.imgur.com/kWzhsIu.png)

## [Downloads](https://github.com/ripmeapp/ripme/releases)

Download `ripme.jar` from the [latest release](https://github.com/ripmeapp/ripme/releases).

**Note: If you're currently using version 1.2.x or 1.3.x, you will not automatically get updates to the newest versions. We recommend downloading the latest version from the link above.**

For information about running the `.jar` file, see [the How To Run wiki](https://github.com/ripmeapp/ripme/wiki/How-To-Run-RipMe).

## [Changelog](https://github.com/ripmeapp/ripme/blob/master/ripme.json) (ripme.json)

## [Website](http://rip.rarchives.com/)

# Features

* Quickly downloads all images in an online album (see supported sites below)
* Easily re-rip albums to fetch new content

## [List of Supported Sites](https://github.com/ripmeapp/ripme/wiki/Supported-Sites)

* imgur
* twitter
* tumblr
* instagram
* flickr
* photobucket
* reddit
* gonewild
* motherless
* imagefap
* imagearn
* seenive
* vinebox
* 8muses
* deviantart
* xhamster
* (more)

## Not Supported?

Request support for more sites by adding a comment to [this Github issue](https://github.com/RipMeApp/ripme/issues/38).

If you're a developer, you can add your own Ripper by following the wiki guide
[How To Create A Ripper for HTML Websites](https://github.com/ripmeapp/ripme/wiki/How-To-Create-A-Ripper-for-HTML-websites).

# Compiling & Building

The project uses [Maven](http://maven.apache.org/).
To build the .jar file using Maven, navigate to the root project directory and run:

```bash
mvn clean compile assembly:single
```

This will include all dependencies in the JAR.

# Running Tests

After building you can run tests by running the following:

```bash
mvn test
```

Please note that some tests may fail as sites change and our rippers become out of date.
Start by building and testing a released version of RipMe
and then ensure that any changes you make do not cause more tests to break.

# Dependencies

* junit-3.8.1
* jsoup-1.8.1
* json-20140107
* apache-commons-configuration-1.7
* log4j-1.2.17
* commons-cli-1.2
* commons-io-1.3.2
* httpcomponents-4.3.3

dummy edit to test
