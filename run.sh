#!/bin/bash
DOCKER_BUILD=${1-"NO"}
if [ "$DOCKER_BUILD" = "YES" ]; then
    docker build . -t decision-tree-core
fi
docker run --env=NEO4J_AUTH=none --publish=7474:7474 --publish=7687:7687 decision-tree-core

