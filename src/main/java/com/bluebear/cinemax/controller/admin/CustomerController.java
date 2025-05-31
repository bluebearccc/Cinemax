package com.bluebear.cinemax.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerController {

    @GetMapping(value = "/index")
    public String indexPage(Model model) {
        return "index";
    }
}
