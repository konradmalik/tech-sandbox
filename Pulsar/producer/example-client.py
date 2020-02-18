import pulsar

client = pulsar.Client('pulsar://pulsar:6650')
producer = client.create_producer('my-topic')

for i in range(100):
    producer.send(('hello-pulsar-%d' % i).encode('utf-8'))

client.close()
