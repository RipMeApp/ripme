Ripme
=====

Album ripper for various websites. Runs on your computer. Requires Java 1.6

[Download v1.x](http://rarchives.com/ripme.jar) (ripme.jar)
--------------------------

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

Request more sites [here](https://github.com/4pr0n/ripme/issues/8)

Compiling & Building
--------------------

The project uses [Maven](http://maven.apache.org/). To build the .jar file using Maven, navigate to the root project directory and run:

```bash
mvn clean compile assembly:single
```

This will include all dependencies in the JAR.
