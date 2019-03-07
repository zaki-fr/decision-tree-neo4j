docker build . -t decision-tree-core
docker run --env=NEO4J_AUTH=none --publish=7474:7474 --publish=7687:7687 decision-tree-core

