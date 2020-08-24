package com.aastha.communication.message.producer;

import com.aastha.communication.config.JmsConfig;
import com.aastha.communication.message.dto.JmsBookingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingMessageProducer {

    @Autowired
    private JmsMessagingTemplate messagingTemplate;

    private static final String BOOKING_REQUEST_GENERATE = "booking-request-generate";

    public static final String DESTINATION =
        JmsConfig.buildVirtualTopic(BOOKING_REQUEST_GENERATE);

    public void requestBooking(JmsBookingRequest request) {
        messagingTemplate.convertAndSend(DESTINATION, request);
    }
}
