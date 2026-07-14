package com.urlshortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/test-error")
public class TestErrorController {

    @GetMapping("/400")
    public void trigger400() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request Test");
    }

    @GetMapping("/401")
    public void trigger401() {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized Test");
    }

    @GetMapping("/403")
    public void trigger403() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden Test");
    }

    @GetMapping("/404")
    public void trigger404() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found Test");
    }

    @GetMapping("/429")
    public void trigger429() {
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests Test");
    }

    @GetMapping("/500")
    public void trigger500() {
        throw new RuntimeException("Server Error Test");
    }
}
