from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
import json
import time
import sys

KAFKA_HOST = sys.argv[1]+':9094'
print('kafka host: ' + KAFKA_HOST)

try:
    consumer = KafkaConsumer(
    'test-local',
        group_id='my-group-2',
        bootstrap_servers=KAFKA_HOST,
        value_deserializer=lambda m: json.loads(m.decode('utf-8')))
except NoBrokersAvailable:
    sys.exit('broker not available (yet?)')

if not consumer.bootstrap_connected():
    sys.exit('not connected, restarting...')

try:
    print('consuming')
    for message in consumer:
        # message value and key are raw bytes -- decode if necessary!
        # e.g., for unicode: `message.value.decode('utf-8')`
        print ("%s:%d:%d: key=%s value=%s" % (message.topic, message.partition,
                                            message.offset, message.key,
                                            message.value))
    print('finished batch')
    time.sleep(1)
except Exception as e:
    print(repr(e))
    consumer.unsubscribe()
    consumer.close()
    
