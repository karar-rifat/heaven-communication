package com.warpspeedventures.comm.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Locale;

class ElementLocaleDeserializer extends JsonDeserializer<Locale> {

    @Override
    public Locale deserialize(JsonParser parser, DeserializationContext ctxt)
        throws IOException {
        return Locale.forLanguageTag(parser.getText());
    }
}
