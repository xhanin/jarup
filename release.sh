#!/bin/sh

if [ "$#" -ne 1 ]
then
  echo "Usage: release.sh <version>"
  exit 1
fi

echo "releasing $1"

rm -rf release
mkdir release
cd release
git clone git@github.com:xhanin/jarup.git .

sed -i '' s/0.01-SNAPSHOT/$1/g pom.xml

mvn clean package

cp -f target/jarup-$1.jar dist/jarup.jar

git checkout -- pom.xml
git add .
git commit -m "release $1"
git tag -a $1 -m "release $1"
git push origin
git push --tags origin