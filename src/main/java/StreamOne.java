import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
//import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.StreamsConfig;

import org.apache.kafka.streams.processor.TopologyBuilder;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

public class StreamOne {

    private static Object TestUtils;

    public static void main(String[] args) throws InterruptedException {

        String inputTopic = "StreamInput";
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG,
                "wordcount-live-test");
         String bootstrapServers = "localhost:9092";
        streamsConfiguration.put(
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        streamsConfiguration.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        streamsConfiguration.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

 //       streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getAbsolutePath());

        KStreamBuilder builder = new KStreamBuilder();
        KStream<String, String> textLines = builder.stream(inputTopic);
        Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

        KTable<String, Long> wordCounts = textLines
                .flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
                .groupBy((key, word) -> word)
                .count();

        wordCounts
                .foreach((w, c) -> System.out.println("word: " + w + " -> " + c));

        String outputTopic = "StreamOutput";
        Serde<String> stringSerde = Serdes.String();
        Serde<Long> longSerde = Serdes.Long();
        wordCounts.to(stringSerde, longSerde, outputTopic);

        KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
        streams.start();

        Thread.sleep(30000);
        streams.close();
    }
}
