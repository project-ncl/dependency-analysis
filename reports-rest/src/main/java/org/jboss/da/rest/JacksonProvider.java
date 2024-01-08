package org.jboss.da.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Jackson Provider just to properly serialize the timestamp
 */
@Provider
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_PATCH_JSON })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_PATCH_JSON })
public class JacksonProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper;

    public JacksonProvider() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // write dates in ISO8601 format
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public ObjectMapper getMapper() {
        return objectMapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> objectType) {
        return objectMapper;
    }
}
