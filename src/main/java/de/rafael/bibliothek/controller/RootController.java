package de.rafael.bibliothek.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

/**
 * @author Rafael K.
 * @since 19:38, 12.06.23
 */

@Controller
public class RootController {

    @GetMapping({"/", "/docs"})
    public ResponseEntity<?> displayErrorMessage() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("docs/"))
                .build();
    }

}
