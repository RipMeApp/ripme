# We've moved!

You can now find the latest code, issues, and releases at [RipMeApp/ripme](https://github.com/RipMeApp/ripme/).


# Etiquette

Please be polite and supportive to all users and contributors. Please be inclusive of everyone regardless of race, religion, gender identity or expression, sexual preference, or tools and platform preferences. Please be helpful and stick to the engineering facts, and avoid expressing unhelpful or off-topic opinions.


# NSFW Content

**Please tag NSFW links (links to sites with adult content) with "(NSFW)"!**

Many of the sites we deal with contain NSFW (Not Safe For Work) content. Please assume any link you see is NSFW unless tagged otherwise -- i.e., SFW (Safe For Work). Please tag all links you post with either "(NSFW)" or "(SFW)" to be considerate to others who may not be browsing this repo in private or who are not interested in NSFW content.

There is a helpful plugin called uMatrix available for [Firefox](https://addons.mozilla.org/en-US/firefox/addon/umatrix/) and [Chrome](https://chrome.google.com/webstore/detail/umatrix/ogfcmafjalglgifnmanfmnieipoejdcf) which allows you to block certain types of content like media and scripts.
If you're not sure if a site might contain NSFW images or media, and you are in mixed company but want to develop a new ripper, you can block downloading images and media in the * (all sites) scope and allow requests for specific domains you trust as you go.
Being able to browse the HTML is usually the most important part of developing or fixing a ripper, so it is not necessarily important to actually see the images load.


# Priorities

Our priorities, roughly in order of impact:

* Bug fixes for popular websites (e.g. that have recently changed their layout and broke our behavior).
* Bug fixes for minor websites
* New Rippers
* Refactorings that make development easier
* Style fixes


# Issues

## Bugs

If you have noticed a bug in RipMe, please open an issue at [RipMeApp/ripme](https://github.com/RipMeApp/ripme/issues/new).

Please include enough information that we can easily confirm the problem and verify when it is fixed. (For example: the exact URL that you tried to rip when something bad/incorrect happened.)


## Rippers / Website Support

Request support for more sites by adding a comment to [this Github issue](https://github.com/RipMeApp/ripme/issues/2068).

If you're a developer, you can add your own Ripper by following the wiki guide
[How To Create A Ripper for HTML Websites](https://github.com/ripmeapp/ripme/wiki/How-To-Create-A-Ripper-for-HTML-websites).


# Pull Requests

Before you open your pull request, please consider the following:

* Please ensure your change is based on the `master` branch of this repo (i.e. https://github.com/RipMeApp/ripme.git)
  * Please do `git pull` on the `master` branch before starting work on your bug fix.
  * This helps avoid merge conflicts.
* Please ensure your change includes only the minimum changes needed to fix a single issue. These are easiest to review and tend to get merged more quickly. If the scope of your PR is wider than a single issue, you may be asked to reopen your PR as multiple separate PRs.
* Are you fixing an issue from one of the issue trackers ([RipMeApp](https://github.com/RipMeApp/ripme/issues) or ([RipMeApp2](https://github.com/RipMeApp2/ripme/issues) or [4pr0n](https://github.com/4pr0n/ripme/issues))? If so, please ensure that you reference the issue you are fixing in your commit message so that it will be [automatically closed](https://help.github.com/articles/closing-issues-via-commit-messages/).
* Please ensure you verify that you did not break any functionality outside of your change or feature
  * The CI might be broken, so please ensure that `gradlew test` shows no new errors since before your change.
  * Keep in mind each Ripper likely supports multiple URL formats for each website, which have different content layouts (users, galleries, etc.)
  * We deal with live websites, so things might break while we aren't looking. Websites can change and content can be deleted at any time. Our code and/or tests may need to be rewritten to fix issues.
* Please consider adding a test to check for regressions to the Ripper you added or the bug you fixed.
  * See e.g. `src/test/java/com/rarchives/ripme/tst/ripper/rippers/ImgurRipperTest.java`


## Style

Generally, we will regard style changes as low-priority. Please consider that the contributors don't have a lot of volunteer time to work on this project, so style changes which do not improve the functionality of the project may be ignored in favor of critical bug fixes, new features, or other tangible improvements. Additionally, changes which are difficult to review may be ignored.

If you make a large or complex change, please detail what changes you made, and how they are helpful or what they fix.

If you feel the need to make a style change: changes to spacing and so on are easy to review regardless of the number of lines changed if ONLY whitespace changes are present. (`git diff -w` is helpful here.) If you change spacing and layout, please avoid also moving things around or otherwise refactoring the code. **Submit refactoring changes separately from style changes.**

Good style is a tool for communicating your intent with other developers of the project. We are interested in maintaining reasonably well-styled code. If a contribution is illegible, we may refuse to merge it until it has been brought up to reasonable style guidelines. If a contribution violates any of our "rules" but is still legible, it is likely to be merged anyway.

Some recommendations:

* Above all, be consistent!
* Spaces, not tabs. Indents should be 4 spaces.
* We prefer "Egyptian brackets" (in `if`, `for`, `while`, `switch`, etc.):
  * `if (...) {`
  * `} else if (...) {`
  * `} else {`
  * `}`
* Note the spacing convention above for control flow constructs (a single space on the outside of each paren)
* Constants in `UPPER_SNAKE_CASE` a.k.a. `CONST_CASE`
* Class names in `PascalCase` a.k.a. `UpperCamelCase`
* Variable names in `camelCase` a.k.a. `lowerCamelCase`
* Do not use Hungarian notation
* Do not use `lower_snake_case`
* Place spaces around binary operators: `1 + 2` not `1+2`
* Do not place spaces inside of parens: `(a + b)` not `( a + b )`
* Use a function like VS Code's "Organize Imports" to ensure imports are committed to the repo in a consistent order no matter who writes the code.
