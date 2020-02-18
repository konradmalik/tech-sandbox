#/bin/bash

docker run --rm -it \
	--name rabbitmq \
	--hostname rabbitmq \
	--publish 5672:5672 \
	--publish 1883:1883 \
	--publish 8080:15672 \
	rabbitmq-management-mqtt
