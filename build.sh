#!/usr/bin/env bash
mvn clean compile assembly:single
# Strip the jar of any non-reproducible metadata such as timestamps
mvn io.github.zlika:reproducible-build-maven-plugin:0.6:strip-jar