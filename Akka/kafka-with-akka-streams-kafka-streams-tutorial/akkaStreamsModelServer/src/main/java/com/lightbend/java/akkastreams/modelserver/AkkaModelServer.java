package com.lightbend.java.akkastreams.modelserver;

import akka.actor.ActorSystem;
import akka.japi.function.Predicate;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.lightbend.java.configuration.kafka.ApplicationKafkaParameters;
import com.lightbend.java.model.DataConverter;
import com.lightbend.java.model.ModelWithDescriptor;
import com.lightbend.model.Winerecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.util.Arrays;
import java.util.Optional;

/**
 * Entry point for the Akka model serving example.
 */
public class AkkaModelServer {

    private static void help(String message, int exitCode) {
        System.out.printf("%s\n", message);
        System.out.printf("AkkaModelServer -h | --help | c | custom | a | actor\n\n");
        System.out.printf("-h | --help   Print this help and exit\n");
        System.out.printf(" c | custom   Use the custom stage implementation (default)\n");
        System.out.printf(" a | actor    Use the actors implementation\n\n");
        System.exit(exitCode);
    }

    public static class WineRecordFailedChecker implements akka.japi.function.Predicate<Optional<Winerecord.WineRecord>> {
        public boolean test(Optional<Winerecord.WineRecord> o) {
            return ! o.isPresent();
        }
    }

    public static void main(String [ ] args) throws Throwable {

        // You can either pick which one to run using a command-line argument, or for ease
        // of use with the IDE Run menu command, just switch which line is commented out.
        ModelServerProcessor.ModelServerProcessorStreamCreator modelServerProcessor =
                new ModelServerProcessor.CustomStageModelServerProcessor();
//                new ModelServerProcessor.ActorModelServerProcessor();

        if (args.length == 0 || args[0].equals("c") || args[0].equals("custom")) {
            // Already set ...
        } else if (args[0].equals("a") || args[0].equals("actor")) {
            modelServerProcessor = new ModelServerProcessor.ActorModelServerProcessor();
        } else if (args[0].equals("-h") || args[0].equals("--help")) {
            help("", 0);
        } else {
            help("Unexpected arguments:" + Arrays.toString(args), 1);
        }

        // Akka
        final ActorSystem system = ActorSystem.create("ModelServing");
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        // Kafka config
        final ConsumerSettings<byte[], byte[]> dataConsumerSettings =
                ConsumerSettings.create(system, new ByteArrayDeserializer(), new ByteArrayDeserializer())
                        .withBootstrapServers(ApplicationKafkaParameters.KAFKA_BROKER)
                        .withGroupId(ApplicationKafkaParameters.DATA_GROUP)
                        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final ConsumerSettings<byte[], byte[]> modelConsumerSettings =
                ConsumerSettings.create(system, new ByteArrayDeserializer(), new ByteArrayDeserializer())
                        .withBootstrapServers(ApplicationKafkaParameters.KAFKA_BROKER)
                        .withGroupId(ApplicationKafkaParameters.MODELS_GROUP)
                        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        // Data stream
        Source<Winerecord.WineRecord, Consumer.Control> dataStream =
                Consumer.atMostOnceSource(dataConsumerSettings, Subscriptions.topics(ApplicationKafkaParameters.DATA_TOPIC))
                        .map(record -> DataConverter.convertData(record.value()))
                        .divertTo(Sink.foreach(x -> System.out.println("FAILED TO PARSE DATA RECORD"+x)),
                                new WineRecordFailedChecker()) // If the parse failed, print an error message!
                        .filter(record -> record.isPresent()).map(record ->record.get());

        // Model stream
        Source<ModelWithDescriptor, Consumer.Control> modelStream =
        Consumer.atMostOnceSource(modelConsumerSettings, Subscriptions.topics(ApplicationKafkaParameters.MODELS_TOPIC))
                .map(record -> DataConverter.convertModel(record.value()))
                .filter(record -> record.isPresent()).map(record -> record.get())
                .map(record -> DataConverter.convertModel(record))
                .filter(record -> record.isPresent()).map(record -> record.get());

        // Exercise:
        // Like all good production code, we're ignoring errors in the `dataStream` and `modelStream` code, when any of the
        // map steps fail. ;)
        // Duplicate the logic shown for `dataStream` to handle the two possible `map` places where errors could occur.
        // See the implementation of `DataConverter`, where we inject fake errors. Add the same logic for models there.

        // Use custom stage or actor to serve the model
        modelServerProcessor.createStreams(dataStream, modelStream, system, materializer);
    }
}
