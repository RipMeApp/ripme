# RipMe

[![Licensed under the MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](/LICENSE.txt)
[![Join the chat at https://gitter.im/RipMeApp/Lobby](https://badges.gitter.im/RipMeApp/Lobby.svg)](https://gitter.im/RipMeApp/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Subreddit](https://img.shields.io/badge/discuss-on%20reddit-blue.svg)](https://www.reddit.com/r/ripme/)
![alt Badge Status](https://github.com/ripmeapp2/ripme/actions/workflows/gradle.yml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/RipMeApp/ripme/badge.svg?branch=main)](https://coveralls.io/github/RipMeApp/ripme?branch=main)

The current active development repo for RipMe is located at [ripmeapp2/ripme](https://github.com/ripmeapp2/ripme/).

RipMe has been maintained with ♥️ and in our limited free time by the following
people, roughly in order from most recent primary developer, with current
activity marked by color of the indicator:

- **[@soloturn](https://github.com/soloturn)** 🟢,
- **[@cyian-1756](https://github.com/cyian-1756)** 🟥,
- **[@kevin51jiang](https://github.com/kevin51jiang)** 🟥,
- **[@MetaPrime](https://github.com/metaprime)** 🟡,
- and its original creator, **[@4pr0n](https://github.com/4pr0n)** 🟥.

If you'd like to become a maintainer, ask an active maintainer to be added to the team.

Chat with the team and community on [gitter](https://gitter.im/RipMeApp/Lobby) and [reddit.com/r/ripme](https://www.reddit.com/r/ripme/)

# About

RipMe is an album ripper for various websites. It is a cross-platform tool that runs on your computer, and
requires Java 21 or later to run. RipMe has been tested and is confirmed working on Windows, Linux, and MacOS.

![Screenshot](https://i.imgur.com/UCQNjeg.png)

## Downloads

Download `ripme.jar` from the [latest release](https://github.com/ripmeapp2/ripme/releases). For information about running the `.jar` file, see
[the How To Run wiki](https://github.com/ripmeapp/ripme/wiki/How-To-Run-RipMe).

The version number like `ripme-1.7.94-17-2167aa34-feature_auto_release.jar` contains a release number (`1.7.94`), given by
a person, the number of commits since this version (`17`). The commit SHA (`2167aa34`) uniquely references the
source code ripme was built from. If it is not built from the main branch, the branch name (`feature/auto-release`) is
given.

## Installation

On macOS, there is a [cask](https://github.com/Homebrew/homebrew-cask/blob/master/Casks/ripme.rb).

```
brew install --cask ripme && xattr -d com.apple.quarantine /Applications/ripme.jar
```

## Changelog

[Changelog](/ripme.json) **(ripme.json)**

# Features

- Quickly downloads all images in an online album. [See supported sites](https://github.com/ripmeapp/ripme/wiki/Supported-Sites)
- Easily re-rip albums to fetch new content
- Built in updater
- Skips already downloaded images by default
- Can auto skip e-hentai and nhentai albums containing certain tags. [See here for how to enable](https://github.com/RipMeApp/ripme/wiki/Config-options#nhentaiblacklisttags)
- Download a range of urls. [See here for how](https://github.com/RipMeApp/ripme/wiki/How-To-Run-RipMe#downloading-a-url-range)

## List of Supported Sites

- imgur
- twitter
- tumblr
- instagram
- flickr
- photobucket
- reddit
- gonewild
- motherless
- imagefap
- imagearn
- seenive
- vinebox
- 8muses
- deviantart
- xhamster
- [(more)](https://github.com/ripmeapp/ripme/wiki/Supported-Sites)

## Not Supported?

Request support for more sites by adding a comment to [this Github issue](https://github.com/RipMeApp/ripme/issues/38).

If you're a developer, you can add your own Ripper by following the wiki guide:
[How To Create A Ripper for HTML Websites](https://github.com/ripmeapp/ripme/wiki/How-To-Create-A-Ripper-for-HTML-websites).

# Compiling & Building

The project uses [Gradle](https://gradle.org). To build the .jar file,
navigate to the root project directory and run at least the test you
change, e.g. Xhamster. test execution can also excluded completely:

```bash
./gradlew clean build testAll --tests XhamsterRipperTest.testXhamster2Album
./gradlew clean build -x test --warning-mode all
```

The generated JAR (java archive) in build/libs will include all
dependencies.

# Running Tests

Tests can be tagged as beeing slow, or flaky. The gradle build reacts to
the following combinations of tags:

- default is to run all tests without tag.
- testAll runs all tests.
- testFlaky runs tests with tag "flaky".
- testSlow runs tests with tag "slow".
- tests can be run by test class, or single test. Use "testAll" so it does
  not matter if a test is tagged or not.

```bash
./gradlew test
./gradlew testAll
./gradlew testFlaky
./gradlew testSlow
./gradlew testAll --tests XhamsterRipperTest
./gradlew testAll --tests XhamsterRipperTest.testXhamster2Album
```

Please note that some tests may fail as sites change and our rippers
become out of date. Start by building and testing a released version
of RipMe and then ensure that any changes you make do not cause more
tests to break.

# New GUI - compose-jb
As Java Swing will go away in future, a new GUI technology should be used. One of the
candidates is [Jetpack Compose for Desktop](https://github.com/JetBrains/compose-jb/).

The library leverages the compose library for android and provides it for android,
desktop and web. The navigation library is not available for desktop, so Arkadii Ivanov
implemented
[decompose](https://proandroiddev.com/a-comprehensive-hundred-line-navigation-for-jetpack-desktop-compose-5b723c4f256e).
