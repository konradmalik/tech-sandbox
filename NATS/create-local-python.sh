#!/bin/bash

python3 -m venv local-nats && \
source local-nats/bin/activate && \
pip install asyncio-nats-client asyncio-nats-streaming && \
deactivate
