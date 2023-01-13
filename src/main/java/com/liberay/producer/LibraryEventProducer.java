package com.liberay.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liberay.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class LibraryEventProducer {


    @Autowired
    private KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String topic="library-events";

    public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key= libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        ListenableFuture<SendResult<Integer, String>> sendResult = kafkaTemplate.sendDefault(key, value);

        sendResult.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

            @Override
            public void onFailure(Throwable ex) {
                handlerFailure(key,value,ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handlerSuccess(key,value,result);

            }
        });
        return  sendResult;
    }


    public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent_Approach2(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key= libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);


        ProducerRecord<Integer,String> producerRecord=buildProducerRecord(key,value,topic);
        
        ListenableFuture<SendResult<Integer, String>> sendResult = kafkaTemplate.send( producerRecord);

        sendResult.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

            @Override
            public void onFailure(Throwable ex) {
                handlerFailure(key,value,ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handlerSuccess(key,value,result);

            }
        });
        return sendResult;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {
        List<Header> recordHeader= List.of(new RecordHeader("event-source","scanner".getBytes(StandardCharsets.UTF_8)));
        return new ProducerRecord<>(topic,null,key,value,recordHeader);
    }


    private void handlerFailure(Integer key, String value, Throwable ex) {
        log.error("Error Sending the message key: {}  value is {} and error message is {}", key,value, ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error is onFailure : {} ", throwable.getMessage());
        }
    }

    private void handlerSuccess(Integer key, String value, SendResult<Integer, String> result) {

        log.info("Message send successfully for the key :{} and the value is {} , partition is {}", key
        ,value, (result.getRecordMetadata().partition()));
    }


}
