#!/bin/sh

echo "Building chatr.server and chatr.client..."

ant -f build-server.xml && ant -f build-client.xml
ant -f build-server.xml clean
