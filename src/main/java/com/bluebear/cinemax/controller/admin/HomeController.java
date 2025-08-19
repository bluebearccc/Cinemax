package com.bluebear.cinemax.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("adminhomee")
@RequestMapping("/admin") // Thay đổi ở đây
public class HomeController {
    @GetMapping // URL bây giờ sẽ là /admin
    public String redirectToAdmin() {
        return "admin/dashboard";
    }
}