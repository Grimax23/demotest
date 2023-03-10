package com.example.demotest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TestController {

    @GetMapping(value = "/get")
    @Throttling
    public ResponseEntity get() {
        return ResponseEntity.ok().body(null);
    }

    @GetMapping(value = "/testIpThrottler/{ip}")
    @Throttling
    public ResponseEntity testIpThrottler(@PathVariable(value = "ip") String ip) {
        return ResponseEntity.ok().body(null);
    }

}