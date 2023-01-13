package com.liberay.integration.controller;

import com.liberay.domain.Book;
import com.liberay.domain.LibraryEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"},partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    private Consumer consumer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;


    @BeforeEach
    void setUp() {
        Map<String, Object> config=new HashMap<>(KafkaTestUtils.consumerProps("groupId", "true",embeddedKafkaBroker));
        consumer=new DefaultKafkaConsumerFactory<>(config,new IntegerDeserializer(),new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();

    }

    @Test
    @Timeout(3)
    public void postLibraryEvent(){

        Book book=Book.builder()
                .bookId(123)
                .bookName("Mystery of life")
                .bookAuthor("Robert jems")
                .build();
        LibraryEvent libraryEvent=LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        HttpHeaders headers=new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());

        HttpEntity<LibraryEvent> request=new HttpEntity<>(libraryEvent,headers);


        ResponseEntity<LibraryEvent> libraryevent = restTemplate.exchange("/libraryevent", HttpMethod.POST, request, LibraryEvent.class);
        Assertions.assertEquals(HttpStatus.CREATED,libraryevent.getStatusCode());

        ConsumerRecord<Integer,String> singleRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");

        Assertions.assertEquals(singleRecord.value().contains("Mystery of life"),true );
    }
}
