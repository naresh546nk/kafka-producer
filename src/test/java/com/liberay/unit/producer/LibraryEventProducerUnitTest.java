package com.liberay.unit.producer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liberay.domain.Book;
import com.liberay.domain.LibraryEvent;
import com.liberay.producer.LibraryEventProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.types.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {


    @InjectMocks
    LibraryEventProducer libraryEventProducer;

    @Spy
    ObjectMapper objectMapper;

    @Mock
    private KafkaTemplate<Integer,String> kafkaTemplate;

    @Test
    public void sendLibraryEvent_Approach2_onFailure() throws JsonProcessingException {
        Book book=Book.builder()
                .bookId(123)
                .bookName("Mystery of life")
                .bookAuthor("Robert jems")
                .build();
        LibraryEvent libraryEvent=LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();


        SettableListenableFuture future=new SettableListenableFuture();
        future.setException(new RuntimeException("Exception calling Kafka"));

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

       assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent).get());
    }


    @Test
    public void sendLibraryEvent_Approach2_onSuccess() throws JsonProcessingException, ExecutionException, InterruptedException {
        Book book=Book.builder()
                .bookId(123)
                .bookName("Mystery of life")
                .bookAuthor("Robert jems")
                .build();
        LibraryEvent libraryEvent=LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer,String > producerRecord=new ProducerRecord<>("library-events",libraryEvent.getLibraryEventId(),value);
        TopicPartition topicPartition = new TopicPartition("libray-events", 3);
        RecordMetadata metadata=new RecordMetadata(topicPartition,1,2,System.currentTimeMillis(),2,3);
        SendResult<Integer, String> sendResult=new SendResult<>(producerRecord,metadata);




        SettableListenableFuture future=new SettableListenableFuture();
        future.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent);
        SendResult<Integer, String> result = listenableFuture.get();
        assertEquals(result.getRecordMetadata().partition(),3);

    }

}
