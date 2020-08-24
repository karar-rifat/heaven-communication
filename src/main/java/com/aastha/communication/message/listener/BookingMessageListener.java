package com.aastha.communication.message.listener;

import com.aastha.communication.config.JmsConfig;
import com.aastha.communication.message.dto.JmsBookingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingMessageListener {

    private static final String BOOKING_REQUEST_GENERATE = "booking-request-generate";

    @JmsListener(
        destination = JmsConfig.QUEUE_PREFIX + JmsConfig.CONSUMER + ".Aastha."
            + JmsConfig.VIRTUAL_TOPIC + "." + BOOKING_REQUEST_GENERATE)
    public void processAsyncMessage(JmsBookingRequest message) {
        System.out.println(message.getBookingId());
    }
}
