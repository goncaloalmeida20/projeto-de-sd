package com.example.webserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // This indicates that any properties not bound in this type should be
// ignored.

public record HackerNewsUserRecord (
        String id,
        Long created,
        Integer karma,
        String about,
        List submitted
) {
}
