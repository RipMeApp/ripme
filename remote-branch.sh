#!/usr/bin/bash

# if $1 is user:branch, split it into $USER and $BRANCH at the :
if [[ $1 == *:* ]]; then
	echo "Splitting $1 into user and branch"
	USER=$(echo $1 | cut -d: -f1)
	BRANCH=$(echo $1 | cut -d: -f2)
else
	# if $1 and $2 are not both specified, fail
	if [ -z "$1" ] || [ -z "$2" ]; then
		echo "Usage: $0 <username> <branch>"
		exit 1
	fi

	# provide defaults for $1 and $2 for safety anyway
	USER=${1:-ripmeapp}
	BRANCH=${2:-main}
fi

# Check that USER and BRANCH are not empty
if [ -z "$USER" ] || [ -z "$BRANCH" ]; then
	echo "Usage: $0 <username> <branch>"
	exit 1
fi

LOCAL_BRANCH=$USER-$BRANCH
REMOTE_BRANCH=$USER/$BRANCH

git remote add $USER https://github.com/$USER/ripme.git
git fetch $USER $BRANCH
git checkout -B $LOCAL_BRANCH $USER/$BRANCH
