# RipMe [![Licensed under the MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/RipMeApp/ripme/blob/master/LICENSE.txt) [![Join the chat at https://gitter.im/RipMeApp/Lobby](https://badges.gitter.im/RipMeApp/Lobby.svg)](https://gitter.im/RipMeApp/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Subreddit](https://img.shields.io/badge/discuss-on%20reddit-blue.svg)](https://www.reddit.com/r/ripme/)

[![Build Status](https://travis-ci.org/RipMeApp/ripme.svg?branch=master)](https://travis-ci.org/RipMeApp/ripme)
[![Coverage Status](https://coveralls.io/repos/github/RipMeApp/ripme/badge.svg?branch=master)](https://coveralls.io/github/RipMeApp/ripme?branch=master)

# Contribute

RipMe is maintained with ♥️ and in our limited free time by **[@MetaPrime](https://github.com/metaprime)** and **[@cyian-1756](https://github.com/cyian-1756)**. If you'd like to contribute but aren't good with code, help keep us happy with a small contribution!

[![Tip with PayPal](https://img.shields.io/badge/PayPal-Buy_us...-lightgrey.svg)](https://www.paypal.me/ripmeapp)
[![Tip with PayPal](https://img.shields.io/badge/coffee-%245-green.svg)](https://www.paypal.com/paypalme/ripmeapp/send?amount=5.00&currencyCode=USD&locale.x=en_US&country.x=US)
[![Tip with PayPal](https://img.shields.io/badge/beer-%2410-yellow.svg)](https://www.paypal.com/paypalme/ripmeapp/send?amount=10.00&currencyCode=USD&locale.x=en_US&country.x=US)
[![Tip with PayPal](https://img.shields.io/badge/lunch-%2420-orange.svg)](https://www.paypal.com/paypalme/ripmeapp/send?amount=20.00&currencyCode=USD&locale.x=en_US&country.x=US)
[![Tip with PayPal](https://img.shields.io/badge/dinner-%2450-red.svg)](https://www.paypal.com/paypalme/ripmeapp/send?amount=50.00&currencyCode=USD&locale.x=en_US&country.x=US)
[![Tip with PayPal](https://img.shields.io/badge/custom_amount-...-lightgrey.svg)](https://www.paypal.me/ripmeapp)

# About

RipMe is an album ripper for various websites. Runs on your computer. Requires Java 8.

![Screenshot](http://i.imgur.com/kWzhsIu.png)

## [Downloads](https://github.com/ripmeapp/ripme/releases)

Download `ripme.jar` from the [latest release](https://github.com/ripmeapp/ripme/releases).

**Note: If you're currently using version 1.2.x or 1.3.x, you will not automatically get updates to the newest versions. We recommend downloading the latest version from the link above.**

For information about running the `.jar` file, see [the How To Run wiki](https://github.com/ripmeapp/ripme/wiki/How-To-Run-RipMe).

## [Changelog](https://github.com/ripmeapp/ripme/blob/master/ripme.json) (ripme.json)

# Features

* Quickly downloads all images in an online album (see supported sites below)
* Easily re-rip albums to fetch new content
* Built in updater
* Can rip images from tumblr in the size they were uploaded in [See here for how to enable](https://github.com/RipMeApp/ripme/wiki/Config-options#tumblrget_raw_image)
* Skips already downloaded images by default

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
