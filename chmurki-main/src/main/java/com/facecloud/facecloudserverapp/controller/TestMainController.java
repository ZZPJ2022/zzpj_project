package com.facecloud.facecloudserverapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestMainController {

    @GetMapping()
    public String helloWorld() {
        return "Hello World!";
    }
}
