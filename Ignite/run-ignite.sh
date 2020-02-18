#!/bin/bash

docker run -it --rm \
    -v /home/konrad/Science/Programming/Ignite/example.xml:/example.xml:ro \
    -e "CONFIG_URI=file:///example.xml" \
    -p "10800:10800" \
    -p "11211:11211" \
    -p "47100:47100" \
    -p "47400:47400" \
    -p "47500:47500" \
    -p "8080:8080" \
    -p "49128:49128" \
    -p "31100-31200:31100-31200" \
    -p "48100-48200:48100-48200" \
    apacheignite/ignite
