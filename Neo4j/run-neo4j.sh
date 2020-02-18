#!/bin/bash

docker run --rm -it \
    --name neo4j \
    --publish 7474:7474 \
    --publish 7687:7687 \
    -e NEO4J_AUTH=none \
    neo4j:3.5.6

