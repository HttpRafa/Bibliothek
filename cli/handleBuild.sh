#!/bin/bash

# Load the .env file
export $(cat .env | xargs)

# Check if all four arguments are provided
if [ $# -ne 4 ]; then
  echo "Usage: $0 <project> <version> <build> <commit>"
  exit 1
fi

# Assign arguments to variables
PROJECT=$1
VERSION=$2
BUILD=$3
COMMIT=$4

PROJECT_NAME=${PROJECT^}
VERSION_GROUP=${VERSION%.*}
REPO_URL=$REPO$PROJECT

echo "Project Name: $PROJECT_NAME"
echo "Version: $VERSION_GROUP/$VERSION"
if [ $BUILD -eq -1 ]; then
    echo "Build: AUTO"
else
    echo "Build: $BUILD"
fi
echo "Commit: $COMMIT"
echo "Repository URL: $REPO_URL"

# Clone the git repository
git clone -n $REPO_URL ./repo
git -C ./repo checkout $COMMIT

DOWNLOADS=""

if [ -d "./files/" ]; then
  for file in ./files/*.jar; do
    echo "Generating sha256 hash..."
    SHA256=$(sha256sum $file | cut -d ' ' -f 1)

	  echo "Adding download string to downloads"
	  DOWNLOADS+="--download=application:/app/files/$(basename $file):$SHA256 "

    # I haven't decided how to handle multiple files
    break
  done
fi

echo $DOWNLOADS

# Launch a docker container that can see the mongo database and run the insertBuild.js script
docker run --rm --network="$NETWORK" -e MONGODB_URL=$MONGODB_URL -v $(pwd):/app -v $(pwd)/repo:/repo -v $STORAGE_DIR:/storage -v $(pwd)/.gitconfig:/root/.gitconfig node:latest node /app/insertBuild.js \
	--projectName=$PROJECT \
	--projectFriendlyName=$PROJECT_NAME \
	--versionGroup=$VERSION_GROUP \
	--versionName=$VERSION \
	--buildNumber=$BUILD \
	--repositoryPath=/repo \
	--storagePath=/storage \
	$DOWNLOADS

echo "Cleaning up..."
rm -rf ./files/*

cd ./repo/
rm -rf rm -rf ..?* .[!.]* *