#!/bin/bash

source local-kafka/bin/activate && \
python3 -u local-consumer.py $1
