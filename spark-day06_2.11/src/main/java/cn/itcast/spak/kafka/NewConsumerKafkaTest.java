package cn.itcast.spak.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;


public class NewConsumerKafkaTest {
    public static void main(String[] args) {
        // 消费Kafka topic数据的配置信息
        Properties props = new Properties();
        props.put("bootstrap.servers",
                "node01:9092,node02:9092,node03:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // 构建KafkaConsumer实例对象，读取数据
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // 从哪些Topic中读取数据
        consumer.subscribe(Arrays.asList("order"));

        while (true) {
            // 从Topic中拉取数据
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("topic: " + record.topic() + ", partition: " +
                        record.partition() + ", value: " + record.value() + ", offset: " +
                        record.offset()
                );
            }
        }
    }
}
