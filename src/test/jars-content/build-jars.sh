#!/bin/sh

jar cf example.jar -C example .
cp example.jar example-war/WEB-INF/lib/
jar cf example.war -C example-war .
