# RipMe [![Licensed under the MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/RipMeApp/ripme/blob/master/LICENSE.txt) [![Join the chat at https://gitter.im/RipMeApp/Lobby](https://badges.gitter.im/RipMeApp/Lobby.svg)](https://gitter.im/RipMeApp/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Subreddit](https://img.shields.io/badge/discuss-on%20reddit-blue.svg)](https://www.reddit.com/r/ripme/)

[![Build Status](https://travis-ci.org/RipMeApp/ripme.svg?branch=master)](https://travis-ci.org/RipMeApp/ripme)
[![Coverage Status](https://coveralls.io/repos/github/RipMeApp/ripme/badge.svg?branch=master)](https://coveralls.io/github/RipMeApp/ripme?branch=master)

## Contribute

RipMe is maintained with ♥️ and in our limited free time by **[@MetaPrime](https://github.com/metaprime)**, **[@cyian-1756](https://github.com/cyian-1756)** and **[@kevin51jiang](https://github.com/kevin51jiang)**. If you'd like to contribute but aren't good with code, help keep us happy with a small contribution!

[![Tip with PayPal](https://img.shields.io/badge/PayPal-Buy_us...-lightgrey.svg)](https://www.paypal.me/ripmeapp)
[![Tip with PayPal](https://img.shields.io/badge/coffee-%245-green.svg)](https://www.paypal.com/paypalme/ripmeapp/send?amount=5.00&currencyCode=USD&locale.x=en_US&country.x=US)
[![Tip with PayPal](https://img.shields.io/badge/beer-%2410-yellow.svg)](https://www.paypal.com/paypalme/ripmeapp/send?amount=10.00&currencyCode=USD&locale.x=en_US&country.x=US)
[![Tip with PayPal](https://img.shields.io/badge/lunch-%2420-orange.svg)](https://www.paypal.com/paypalme/ripmeapp/send?amount=20.00&currencyCode=USD&locale.x=en_US&country.x=US)
[![Tip with PayPal](https://img.shields.io/badge/dinner-%2450-red.svg)](https://www.paypal.com/paypalme/ripmeapp/send?amount=50.00&currencyCode=USD&locale.x=en_US&country.x=US)
[![Tip with PayPal](https://img.shields.io/badge/custom_amount-...-lightgrey.svg)](https://www.paypal.me/ripmeapp)

## About

RipMe is an album ripper for various websites. It is a cross-platform tool that runs on your computer, and requires Java 8. RipMe has been tested and confirmed working on Windows, Linux and MacOS. 

![Screenshot](https://i.imgur.com/UCQNjeg.png)

## Downloads

Download `ripme.jar` from the [latest release](https://github.com/ripmeapp/ripme/releases).

**Note: If you're currently using version 1.2.x, 1.3.x or 1.7.49, you will not automatically get updates to the newest versions. We recommend downloading the latest version from the link above.**

For information about running the `.jar` file, see [the How To Run wiki](https://github.com/ripmeapp/ripme/wiki/How-To-Run-RipMe).

## Installation

On macOS, there is a [cask](https://github.com/Homebrew/homebrew-cask/blob/master/Casks/ripme.rb).
```
brew install --cask ripme && xattr -d com.apple.quarantine /Applications/ripme.jar
```

## Changelog

[Changelog](https://github.com/ripmeapp/ripme/blob/master/ripme.json) **(ripme.json)**

# Features

* Quickly downloads all images in an online album. [See supported sites](https://github.com/ripmeapp/ripme/wiki/Supported-Sites)
* Easily re-rip albums to fetch new content
* Built in updater
* Skips already downloaded images by default
* Can auto-skip e-hentai and nhentai albums containing certain tags. [See here for how to enable](https://github.com/RipMeApp/ripme/wiki/Config-options#nhentaiblacklisttags)
* Download a range of URLs. [See here for how](https://github.com/RipMeApp/ripme/wiki/How-To-Run-RipMe#downloading-a-url-range)

## List of Supported Sites

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
* [(more)](https://github.com/ripmeapp/ripme/wiki/Supported-Sites)

## Not Supported?

Request support for more sites by adding a comment to [this Github issue](https://github.com/RipMeApp/ripme/issues/38).

If you're a developer, you can add your own Ripper by following the wiki guide:
[How To Create A Ripper for HTML Websites](https://github.com/ripmeapp/ripme/wiki/How-To-Create-A-Ripper-for-HTML-websites).

# Compiling & Building

The project uses [Gradle](https://gradle.org) or [Maven](http://maven.apache.org/).
Therefor both commands are given. To build the .jar file, navigate to the root
project directory and run:

```bash
mvn clean compile assembly:single
mvn -B package assembly:single -Dmaven.test.skip=true
```
```bash
./gradlew clean build
./gradlew clean build -x test --warning-mode all
```

This will include all dependencies in the JAR. One can skip executing the tests
as well.

# Running Tests

Tests can be marked as beeing slow, or flaky. Default is to run all but the flaky tests. Slow tests can be excluded to
run. slow and flaky tests can be run on their own. After building you can run tests, quoting might be necessary depending
on your shell:

```bash
mvn test
mvn test -DexcludedGroups= -Dgroups=flaky,slow
mvn test '-Dgroups=!slow'
```

```bash
./gradlew test
./gradlew test -DexcludeTags= -DincludeTags=flaky,slow
./gradlew test '-DincludeTags=!slow'
```

Please note that some tests may fail as sites change and our rippers become out of date.
Start by building and testing a released version of RipMe
and then ensure that any changes you make do not cause more tests to break.
