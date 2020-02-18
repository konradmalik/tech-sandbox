package com.lightbend.java.kafkastreams.store.store.custom;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.internals.ProcessorStateManager;
import org.apache.kafka.streams.processor.internals.RecordCollector;
import org.apache.kafka.streams.state.StateSerdes;

/**
 * Log model state changes. Based on this example,
 * https://github.com/confluentinc/examples/blob/3.2.x/kafka-streams/src/main/scala/io/confluent/examples/streams/algebird/CMSStoreChangeLogger.scala
 * Created by boris on 7/11/17.
 */
public class ModelStateStoreChangeLogger<K,V> {

    private String topic;
    private RecordCollector collector;
    private int partition;
    private StateSerdes<K, V> serialization;
    private ProcessorContext context;

    public ModelStateStoreChangeLogger(String storeName, ProcessorContext context, int partition, StateSerdes<K, V> serialization){
        topic = ProcessorStateManager.storeChangelogTopic (context.applicationId(), storeName);
        collector = ((RecordCollector.Supplier)context).recordCollector();
        this.partition = partition;
        this.serialization = serialization;
        this.context = context;
    }

    public ModelStateStoreChangeLogger(String storeName, ProcessorContext context, StateSerdes<K, V> serialization){
        this(storeName, context, context.taskId().partition, serialization);
    }

    public void logChange(K key, V value) {
        if (collector != null) {
            Serializer<K> keySerializer = serialization.keySerializer();
            Serializer<V> valueSerializer = serialization.valueSerializer();
            long ts = 0;
            try {
                ts = context.timestamp();
            }
            catch (Throwable t){
                ts = System.currentTimeMillis();
            }
            collector.send(this.topic, key, value, this.partition, ts, keySerializer, valueSerializer);
        }
    }
}
