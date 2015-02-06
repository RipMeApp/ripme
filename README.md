RipMe [![Build Status](https://travis-ci.org/4pr0n/ripme.svg?branch=master)](https://travis-ci.org/4pr0n/ripme)
=====

Album ripper for various websites. Runs on your computer. Requires Java 1.6

![Screenshot](http://i.imgur.com/kWzhsIu.png)

[Download v1.x](http://rarchives.com/ripme.jar) (ripme.jar)
--------------------------
For information about running the `.jar` file, see [the How To Run wiki](https://github.com/4pr0n/ripme/wiki/How-To-Run-RipMe)

[Changelog](http://rarchives.com/ripme.json) (ripme.json)
--------------

Features
---------------

* Quickly downloads all images in an online album (see supported sites below)
* Easily re-rip albums to fetch new content

Supported sites:
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

[Full updated list](https://github.com/4pr0n/ripme/issues/8)

Request more sites by adding a comment to [this Github issue](https://github.com/4pr0n/ripme/issues/8) or by following the wiki guide [How To Create A Ripper for HTML Websites](https://github.com/4pr0n/ripme/wiki/How-To-Create-A-Ripper-for-HTML-websites)

Compiling & Building
--------------------

The project uses [Maven](http://maven.apache.org/). To build the .jar file using Maven, navigate to the root project directory and run:

```bash
mvn clean compile assembly:single
```

This will include all dependencies in the JAR.

Dependencies
------------
* junit-3.8.1
* jsoup-1.7.3
* json-20140107
* apache-commons-configuration-1.7
* log4j-1.2.17
* commons-cli-1.2
* commons-io-1.3.2
* httpcomponents-4.3.3
