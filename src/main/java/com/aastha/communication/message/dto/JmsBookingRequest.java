package com.aastha.communication.message.dto;

import com.aastha.communication.message.JmsTypeId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JmsTypeId("booking.request.creation")
public class JmsBookingRequest implements Serializable {
    Long bookingId;
    Long hikerId;
}
