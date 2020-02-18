#!/bin/bash

docker run --rm -it \
	--name deenv \
    --volume `pwd`/data:/home/deenv/data \
	--publish 4040:4040 \
	--publish 3000:3000 \
	--publish 8080:8080 \
    --ipc host \
	konradmalik/deenv
