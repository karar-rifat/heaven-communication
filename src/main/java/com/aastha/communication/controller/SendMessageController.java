package com.aastha.communication.controller;

import com.aastha.communication.message.dto.SimpleRequest;
import com.aastha.communication.message.producer.SimpleMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
public class SendMessageController {

    @Autowired
    SimpleMessageProducer messageProducer;

    @PostMapping
    @RequestMapping("/simple-topic")
    public String sendMessageToTopic(@RequestBody String message){
        messageProducer.sendMessageToTopic(message);
        return "message sent";
    }

    @PostMapping
    @RequestMapping("/simple-queue")
    public String sendMessageToQueue(@RequestBody String message){
        messageProducer.sendMessageToQueue(message);
        return "message sent";
    }
}
