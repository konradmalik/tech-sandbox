package com.lightbend.scala.kafkastreams.queriablestate

import java.net.InetAddress
import java.util

import com.lightbend.java.configuration.kafka.ApplicationKafkaParameters
import com.lightbend.scala.kafkastreams.store.HostStoreInfo
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.{HostInfo, StreamsMetadata}

import scala.collection.JavaConverters._

/**
 * Looks up StreamsMetadata from KafkaStreams and converts the results
 * into Beans that can be JSON serialized via Jersey.
 * @see https://github.com/confluentinc/examples/blob/3.2.x/kafka-streams/src/main/java/io/confluent/examples/streams/interactivequeries/MetadataService.java
 */
class MetadataService(streams: KafkaStreams) {

  /**
    * Get the metadata for all instances of this Kafka Streams application that currently
    * has the provided store.
    *
    * @param store The store to locate
    * @return List of { @link HostStoreInfo}
    */
  def streamsMetadataForStore(store: String, port: Int): util.List[HostStoreInfo] = { // Get metadata for all of the instances of this Kafka Streams application hosting the store
    val metadata = streams.allMetadataForStore(store).asScala.toSeq match{
      case list if !list.isEmpty => list
      case _ => Seq(new StreamsMetadata(
        new HostInfo("localhost", port),
        new util.HashSet[String](util.Arrays.asList(ApplicationKafkaParameters.STORE_NAME)), util.Collections.emptySet[TopicPartition]))
    }
    mapInstancesToHostStoreInfo(metadata)
  }


  private def mapInstancesToHostStoreInfo(metadatas: Seq[StreamsMetadata]) = metadatas.map(convertMetadata(_)).asJava

  private def convertMetadata(metadata: StreamsMetadata) : HostStoreInfo = {
    val currentHost = metadata.host match{
      case host if host equalsIgnoreCase("localhost") =>
        try{InetAddress.getLocalHost.getHostAddress}
        catch {case t: Throwable => ""}
      case host => host
    }
     new HostStoreInfo(currentHost, metadata.port, metadata.stateStoreNames.asScala.toSeq)
  }
}
