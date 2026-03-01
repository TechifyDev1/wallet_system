package com.wallet_system.wallet.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping
    public String testEndpoint() {
        return "Hellow world from spring";
    }
    @GetMapping("/data")
    public String testDataEndpoint() {
        return "Data endpoint";
    }
}
