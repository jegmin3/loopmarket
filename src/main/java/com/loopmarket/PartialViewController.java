package com.loopmarket;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PartialViewController {

    @GetMapping("/index")
    public String IndexGET() {
        return "main/index";
    }
    
    @GetMapping("/login")
    public String LoginGET() {
    	return "/member/login";
    }
    


}
