#!/bin/bash
trap exit SIGINT
set -xe

BASE_DIR=$(dirname $0)/..
cd $BASE_DIR
BASE_DIR=`pwd`
ZOOKEEPER=localhost:2181
KAFKA_BROKER=localhost:9092

# check if the topic exists. if not, create the topic
EXIST=$($BASE_DIR/deploy/kafka/bin/kafka-topics.sh --describe --topic sample-text --zookeeper $ZOOKEEPER)
if [ -z "$EXIST" ]
  then
    $BASE_DIR/deploy/kafka/bin/kafka-topics.sh --create --zookeeper $ZOOKEEPER --topic sample-text --partition 1 --replication-factor 1
fi

# produce raw data
$BASE_DIR/deploy/kafka/bin/kafka-console-producer.sh --topic sample-text --broker $KAFKA_BROKER < $BASE_DIR/sample-text.txt
