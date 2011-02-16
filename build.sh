#!/bin/sh

echo "Building chatr.server and chatr.client..."

ant -f build-server.xml clean
ant -f build-server.xml && ant -f build-client.xml
