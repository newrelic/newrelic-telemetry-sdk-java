package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpanTest {

    @Test
    void testWithError() {
        Span span = Span.builder("123").withError().build();
        assertTrue(span.isError());
    }
    
    @Test
    void testWithErrorDescription() {
        Span span = Span.builder("123").withError("kaboom").build();
        assertTrue(span.isError());
        assertEquals("kaboom", span.getAttributes().asMap().get("error.message"));
    }

    @Test
    void testWithErrorDescriptionAndClass() {
        Span span = Span.builder("123").withError("kaboom", "timeout").build();
        assertTrue(span.isError());
        assertEquals("kaboom", span.getAttributes().asMap().get("error.message"));
        assertEquals("timeout", span.getAttributes().asMap().get("error.class"));
    }

    @Test
    void testAttrsOverwriteWithError() {
        Attributes attrs = new Attributes().put("error.message", "msg").put("error.class", "cls");
        Span span = Span.builder("123")
                .withError("kaboom", "timeout")
                .attributes(attrs)
                .build();
        assertEquals("msg", span.getAttributes().asMap().get("error.message"));
        assertEquals("cls", span.getAttributes().asMap().get("error.class"));
    }

    @Test
    void testWithErrorOverwritesAttrs() {
        Attributes attrs = new Attributes().put("error.message", "msg").put("error.class", "cls");
        Span span = Span.builder("123")
                .attributes(attrs)
                .withError("kaboom", "timeout")
                .build();
        assertEquals("kaboom", span.getAttributes().asMap().get("error.message"));
        assertEquals("timeout", span.getAttributes().asMap().get("error.class"));
    }
}