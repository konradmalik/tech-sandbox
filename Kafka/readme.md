# Run Kafka in a container for internal and external traffic

Below is a simple example on how to run dockerized kafka (here on docker compose but will be analogous on docker swarm or kubernetes) in such a way, that allows both, free traffic inside the clustes (container to container) and from external sources to the container (remote machine to container).

Make sure newest docker and at least python 3.3 is installed and available under **python** command.

Also, check what your distro is using for python virtual environments. In Arch it is **venv**, if you are using **virtualenv**, then modify "create-local-python.sh" script accordingly.

I assume command python3 exists and links to python 3.x interpreter. If not, change it in shell scripts.

## set up all

```bash
$ docker-compose build
```

```bash
$ ./create-local-python.py
```

## run docker and internal (dockerized) producer and consumer
```bash
$ docker-compose up
```

Give if up to 15 seconds (after images are downloaded). Produced and consumed messages 

## check ip of the kafka container

```bash
$ # assuming that kafka container is kafka-docker-internal-external_kafka_1
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' kafka-docker-internal-external_kafka_1
```

## run local producer and consumer (be patient here!)

#### assume that kafka ip is 1.2.3.4

```bash
$ #assuming that kafka ip is 1.2.3.4
$ ./run-local-consumer.sh 1.2.3.4
```

```bash
$ #assuming that kafka ip is 1.2.3.4
$ ./run-local-producer.sh 1.2.3.4
```

## after test, remember to cleanup
```bash
$ docker-compose down
```

# Additional info:

When trying to run consumer and producer from a remote machine, there are two changes:

* change _{HOSTNAME_COMMAND} in docker-compose.yaml to the actual IP of the machine that kafka will run on

* when running local python producers and consumers, specify the kafka machine ip as an argument

* if you have problems with connection, try `curl <KAFKA-MACHINE-IP>:9094`. If response is similar to `(52) Empty reply from server`, then general connectivity works. If reponse is different, it may be a sign that machines cannot comunicate through that port.
