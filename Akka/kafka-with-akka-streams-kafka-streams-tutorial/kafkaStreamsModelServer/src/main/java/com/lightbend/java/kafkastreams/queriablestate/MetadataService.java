package com.lightbend.java.kafkastreams.queriablestate;

import com.lightbend.java.configuration.kafka.ApplicationKafkaParameters;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.StreamsMetadata;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Looks up StreamsMetadata from KafkaStreams and converts the results
 * into Beans that can be JSON serialized via Jersey.
 * @see https://github.com/confluentinc/examples/blob/3.2.x/kafka-streams/src/main/java/io/confluent/examples/streams/interactivequeries/MetadataService.java
 */
public class MetadataService {

    private final KafkaStreams streams;

    public MetadataService(final KafkaStreams streams) {
        this.streams = streams;
    }


    /**
     * Get the metadata for all instances of this Kafka Streams application that currently
     * has the provided store.
     * @param store   The store to locate
     * @return  List of {@link HostStoreInfo}
     */
    public List<HostStoreInfo> streamsMetadataForStore(final  String store, int port) {
        // Get metadata for all of the instances of this Kafka Streams application hosting the store
        Collection<StreamsMetadata> metadata = streams.allMetadataForStore(store);
        if (metadata.isEmpty())
            metadata = Arrays.asList(new StreamsMetadata(new HostInfo("localhost", port),
                        new HashSet<>(Arrays.asList(ApplicationKafkaParameters.STORE_NAME)),
                        Collections.emptySet()));
        return mapInstancesToHostStoreInfo(metadata);
    }


    private List<HostStoreInfo> mapInstancesToHostStoreInfo(final Collection<StreamsMetadata> metadatas) {

        return metadatas.stream().map(metadata -> convertMetadata(metadata)).collect(Collectors.toList());
    }

    private HostStoreInfo convertMetadata(StreamsMetadata metadata){
        String currentHost = metadata.host();
        try {
            if (currentHost.equalsIgnoreCase("localhost"))
                currentHost = InetAddress.getLocalHost().getHostAddress();
        }catch (Throwable t){}
        return new HostStoreInfo(currentHost, metadata.port(), metadata.stateStoreNames());
    }
}
