package com.bluebear.cinemax.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    /**
     * Trang Dashboard chính - Đơn giản
     */
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard - BlueBear Cinema");
        return "admin/index";
    }
}