#!/bin/bash

docker run -it --rm \
    -e "MINIO_ACCESS_KEY=minio" \
    -e "MINIO_SECRET_KEY=minio123" \
    -p "9000:9000" \
    minio/minio \
        server /data
