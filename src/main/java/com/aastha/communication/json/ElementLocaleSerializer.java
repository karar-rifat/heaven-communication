package com.warpspeedventures.comm.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Locale;

class ElementLocaleSerializer extends JsonSerializer<Locale> {
    @Override
    public void serialize(Locale value, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {
        generator.writeString(value.toLanguageTag());
    }
}
