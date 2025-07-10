package com.lfg.lfg_backend.controller;

import com.lfg.lfg_backend.util.EventTagWhitelist;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Espone la lista dei tag consentiti per la creazione/modifica evento.
 * Endpoint: GET /tags
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    @GetMapping
    public ResponseEntity<Set<String>> getAllowedTags() {
        return ResponseEntity.ok(EventTagWhitelist.ALLOWED_TAGS);
    }
}
