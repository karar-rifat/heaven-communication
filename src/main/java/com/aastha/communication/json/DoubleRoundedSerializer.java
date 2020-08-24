package com.aastha.communication.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

class DoubleRoundedSerializer extends JsonSerializer<Double> {

    private final int scale;

    public DoubleRoundedSerializer(int scale) {
        this.scale = scale;
    }

    @Override
    public void serialize(Double value, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {
        generator.writeNumber(BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP));
    }
}
