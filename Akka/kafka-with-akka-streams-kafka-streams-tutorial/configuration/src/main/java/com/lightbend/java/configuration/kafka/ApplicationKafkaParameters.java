package com.lightbend.java.configuration.kafka;

/* Created by boris on 5/18/17. */

/**
 * A set of configuration parameters for running applications.
 */
public class ApplicationKafkaParameters {

    private ApplicationKafkaParameters(){}

    public static final String KAFKA_BROKER = "localhost:9092";

    public static final String STORE_NAME = "ModelStore";
    public static final int STORE_ID = 42;


    public static final String DATA_TOPIC = "mdata";
    public static final String MODELS_TOPIC = "models";

    public static final String DATA_GROUP = "wineRecordsGroup";
    public static final String MODELS_GROUP = "modelRecordsGroup";

}
