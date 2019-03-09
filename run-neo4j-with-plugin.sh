#!/bin/bash
docker images decision-tree-core > /dev/null
if [ "$?" -ne 0 ] || [ "$1" = "YES" ]; then
    mvn clean install
    docker build . -t decision-tree-core
fi
docker run --env=NEO4J_AUTH=none --publish=7474:7474 --publish=7687:7687 decision-tree-core

