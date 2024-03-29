package com.example.configs;

import com.example.model.FrequencyModel;
import com.example.model.NewsModel;
import com.example.service.NewsAnalyzerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;

@Configuration
@Service
public class AnalyzerKafkaConnection {

    @Value("${topic.mockNewsFeedTopic}")
    private String mockNewsFeedTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NewsAnalyzerService newsAnalyzerService;

    @Autowired
    private FrequencyModel frq;

    @Bean
    public NewTopic topic() {

        return TopicBuilder.name(mockNewsFeedTopic).build();
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {

        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }


    public void sendData(Object eventObject){

        kafkaTemplate().send(mockNewsFeedTopic, eventObject);
    }

    //Receive Mock News for analyze.
    @SneakyThrows(value = {JsonProcessingException.class})
    @KafkaListener(topics = "newsAnalyzerTopic", groupId = "groupId1")
    public void AnalyzerConsumer(ConsumerRecord<String, String> newsEvent) {

        NewsModel newsModel = objectMapper.readValue(newsEvent.value(), NewsModel.class);

        //Ignore received mock news or save it to database.
        NewsModel analyzedNewsModel = newsAnalyzerService.saveOrIgnoreMockNewsByFrequency(newsModel,frq);

        //Send analyzed results to News Feed service.
        if (analyzedNewsModel !=null) sendData(analyzedNewsModel);

    }

}