package com.mtgcs.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/", "/friends", "/search", "/error"})
    public String index() {
        return "forward:/index.html";
    }
}
