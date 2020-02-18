#!/bin/bash

docker run --rm -it \
    --name drill \
    --publish 8047:8047 \
    drill/apache-drill:1.16.0 bash

