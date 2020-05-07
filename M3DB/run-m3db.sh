#!/bin/bash

docker run -it --rm \
    -p "7201:7201" \
    -p "7203:7203" \
    -p "9003:9003" \
    --name m3db \
    quay.io/m3db/m3dbnode:latest
