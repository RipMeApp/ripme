# RipMe

[![Licensed under the MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](/LICENSE.txt)
[![Join the chat at https://gitter.im/RipMeApp/Lobby](https://badges.gitter.im/RipMeApp/Lobby.svg)](https://gitter.im/RipMeApp/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Subreddit](https://img.shields.io/badge/discuss-on%20reddit-blue.svg)](https://www.reddit.com/r/ripme/)
![alt Badge Status](https://github.com/ripmeapp2/ripme/actions/workflows/gradle.yml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/RipMeApp/ripme/badge.svg?branch=main)](https://coveralls.io/github/RipMeApp/ripme?branch=main)

Jump to:
- [List of Supported Sites](https://github.com/RipMeApp/ripme?tab=readme-ov-file#list-of-supported-sites)
- [Site Not Supported?](https://github.com/RipMeApp/ripme?tab=readme-ov-file#site-not-supported)

## Recent development updates

- For a while, the ripmeapp/ripme repo was inactive, but development continued at ripmeapp2/ripme.
- Now, maintainers have been updated and development has been rejoined with ripmeapp/ripme where it will continue.
- You may find a number of stale issues on ripmeapp/ripme and/or on ripmeapp2/ripme until everything is merged back together and statuses are updated.
- The current active development repo for RipMe is located at [ripmeapp/ripme](https://github.com/ripmeapp/ripme/).

## Maintainers

RipMe has been maintained with ♥️ and in our limited free time by the following
people, roughly in order from most recent primary developer, with current
activity marked by color of the indicator:

- **[@soloturn](https://github.com/soloturn)** 🟢,
- **[@cyian-1756](https://github.com/cyian-1756)** 🟥,
- **[@kevin51jiang](https://github.com/kevin51jiang)** 🟥,
- **[@MetaPrime](https://github.com/metaprime)** 🟡,
- and its original creator, **[@4pr0n](https://github.com/4pr0n)** 🟥.

If you'd like to become a maintainer, ask an active maintainer to be added to the team.

## Contact

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

Note that this follows the Semantic Versioning spec (see https://semver.org/),
and uses the feature of the format that adds extra data after the `-` to
provide helpful context so that every commit on every branch has a dintinct
semver version associated with it.

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

See the full list of [Supported Sites](https://github.com/ripmeapp/ripme/wiki/Supported-Sites) in the wiki.

Note: Websites change over time and therefore rippers, which fundamentally depend on website layouts, can break at any time.
Feel free to open an issue if you notice something not working, but please search the list of issues to see if it's already been reported.

The list of supported rippers includes:

- imgur
- twitter (currently broken, needs to be updated for X)
- tumblr
- instagram
- flickr
- photobucket
- reddit
- redgifs
- motherless
- imagefap
- seenive
- 8muses
- deviantart (currently broken by major changes to the site)
- xhamster
- xvideos
- ... and [more](https://github.com/ripmeapp/ripme/wiki/Supported-Sites)!

## Site Not Supported?

Request support for more sites by adding a comment to [this Github issue](https://github.com/RipMeApp/ripme/issues/2068).

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
- tests can give the full stack of an assertion, exception, or error if you pass `--info` to the command

```bash
./gradlew test
./gradlew testAll
./gradlew testFlaky
./gradlew testSlow
./gradlew testAll --tests XhamsterRipperTest
./gradlew testAll --tests XhamsterRipperTest.testXhamster2Album
./gradlew testAll --tests ChanRipperTest --info
```

Please note that some tests may fail as sites change and our rippers
become out of date. Start by building and testing a released version
of RipMe and then ensure that any changes you make do not cause more
tests to break.

# Publishing a New Release

## Create the Release

edit draft release `develop build main` the following way:
1. create a new tag with version from ripme filename, e.g. 2.1.12-7-d0b97acd
1. set the title to same name
1. set previous tag to release tag before, e.g.  2.1.11-20-ca96ce88
1. press `generate release notes` button
1. edit release text as appropriate
1. save

Another version of instructions that worked for @metaprime:
- Push latest-main to the version you will want to publish, and wait a few minutes for automation to finish running the build
- Go to https://github.com/RipMeApp/ripme/releases
- Find development build main
- Click the "Edit" button
- Note the version in the filename for the .jar
- Push a tag with that version number
- Update the tag on the release to that version numbered tag that matches the .jar's name
- Change the title on the release to match
- Uncheck "set as a pre-release"
- Check "set as the latest release"
- Click "publish release"

## Publish the JSON update so the update check will pick up the new release

then, prepare the repo for update check, and next release:
1. edit ripme.json, enter new hash, version and short description, and commit
   1. Get the hash by downloading the file and computing a sha256 hash
1. set the base tag for next release verison calculation, e.g. 2.1.13 on this commit
1. push tag and commit
1. remove old base tag, not needed any more, e.g. 2.1.12
