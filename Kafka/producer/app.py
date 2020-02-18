from kafka import KafkaProducer
from kafka.errors import NoBrokersAvailable

import json
import time
import sys

try:
    producer = KafkaProducer(value_serializer=lambda v: json.dumps(v).encode('utf-8'),
        bootstrap_servers='kafka:9092')
except NoBrokersAvailable:
    sys.exit('broker not available (yet?)')

if not producer.bootstrap_connected():
    sys.exit('not connected, restarting...')

try:
    print('producing')
    while True:
        producer.send('test-docker', value={"hello": "docker-producer"})
        # wait for send IMPORTANT IF NOT IN WHILE LOOP!
        producer.flush()
        time.sleep(1)
        print('produced one message')
except Exception as e:
    print(repr(e))
    producer.flush()
    producer.close()
    


