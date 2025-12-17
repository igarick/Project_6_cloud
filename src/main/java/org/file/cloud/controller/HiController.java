package org.file.cloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HiController {
    @GetMapping("/hello")
    public String  sayHi() {
        return "hello";
    }
}
