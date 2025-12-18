package org.file.cloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HiController {
    @GetMapping("/hello")
    public String  sayHi() {
        return "hello";
    }
}
//
//@RestController
//public class HiController {
//    @GetMapping("/hello")
//    public String sayHi() {
//        return "Hello world!!!!!!!";
//    }
//}