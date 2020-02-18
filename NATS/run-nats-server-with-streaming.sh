#!/bin/bash

docker run -it --rm \
    -p "4222:4222" \
    -p "8222:8222" \
    nats-streaming
