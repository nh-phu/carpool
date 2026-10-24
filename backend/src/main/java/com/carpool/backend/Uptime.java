package com.carpool.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Uptime {

    @GetMapping("/uptime")
    public String health() {
        return "WayMate backend is running!";
    }
}
