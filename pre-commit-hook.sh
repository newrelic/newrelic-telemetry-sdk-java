#!/usr/bin/env bash

set -e

files=$(git diff --cached --name-only --diff-filter=ACM | paste -s -d, -)

./gradlew googleJavaFormat -DgoogleJavaFormat.include="$files"
./gradlew verifyGoogleJavaFormat -DverifyGoogleJavaFormat.include="$files"

git diff --cached --name-only --diff-filter=ACM | xargs git add