#!/bin/sh

if [ ! -f  ~/.jarup/jarup.jar ]; then
    echo "installing jarup"
    mkdir -p ~/.jarup && cd ~/.jarup
    echo "downloading jarup.jar"
    curl -O https://rawgithub.com/xhanin/jarup/master/dist/jarup.jar
    java -jar jarup.jar gen-script

    echo "creating a link in /usr/local/bin for convenient use"
    ln -s `pwd`/jarup  /usr/local/bin/jarup
    echo "jarup installed"
else
    echo "updating jarup"
    cd ~/.jarup
    rm jarup.jar && rm jarup
    echo "downloading jarup.jar"
    curl -O https://rawgithub.com/xhanin/jarup/master/dist/jarup.jar
    java -jar jarup.jar gen-script
    echo "jarup updated"
fi
