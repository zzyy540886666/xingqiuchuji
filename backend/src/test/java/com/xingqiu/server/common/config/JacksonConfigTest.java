package com.xingqiu.server.common.config;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.xingqiu.server.analytics.dto.AnalyticsEventRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonConfigTest {

    @Test
    void objectMapperRejectsUnknownFields() throws Exception {
        var mapper = new JacksonConfig().objectMapper();

        assertThrows(UnrecognizedPropertyException.class, () ->
                mapper.readValue("""
                        {
                          "events": [
                            {
                              "eventName": "page_view",
                              "page": "/pages/home/index",
                              "extraField": "blocked"
                            }
                          ]
                        }
                        """, AnalyticsEventRequest.class));
    }
}
