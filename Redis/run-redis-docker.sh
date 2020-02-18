#/bin/bash

docker run --rm -it \
	--name redis \
	--publish 6379:6379 \
	redis:4.0.14-alpine
