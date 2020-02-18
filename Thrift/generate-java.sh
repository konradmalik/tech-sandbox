#!/bin/bash

docker run -u $(id -u) -v "$PWD:/data" thrift thrift -r -out /data --gen java /data/service.thrift
