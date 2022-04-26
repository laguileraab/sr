package com.ce.sr.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/jacoco")
@Controller
public class JacocoController {
    
    @GetMapping
    public String showjacoco(){
        return "./../../../../../../../target/site/jacoco/index.html";
    }
}
