package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.VoucherDTO;
import com.bluebear.cinemax.entity.Voucher;
import com.bluebear.cinemax.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    // Display all vouchers
    @GetMapping
    public String listVouchers(Model model) {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        VoucherService.VoucherStats stats = voucherService.getVoucherStats();

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Voucher Management");

        // Change this line to match your actual template location
        return "admin/list-voucher"; // If list-voucher.html is directly in admin folder
        // return "admin/vouchers/list-voucher"; // If list-voucher.html is in admin/vouchers folder
    }

    // Search vouchers
    @GetMapping("/search")
    public String searchVouchers(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 Model model) {
        List<Voucher> vouchers = voucherService.searchVouchers(keyword, status);
        VoucherService.VoucherStats stats = voucherService.getVoucherStats();

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("stats", stats);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pageTitle", "Voucher Search Results");

        return "admin/list-voucher"; // Match the same template path as above
    }

    // Show voucher details
    @GetMapping("/{id}")
    public String viewVoucher(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Voucher> voucher = voucherService.getVoucherById(id);
        if (!voucher.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Voucher not found");
            return "redirect:/admin/vouchers";
        }

        model.addAttribute("voucher", voucher.get());
        model.addAttribute("pageTitle", "Voucher Details");

        return "admin/detail-voucher"; // Adjust path as needed
    }

    // Show add voucher form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("voucher", new VoucherDTO());
        model.addAttribute("pageTitle", "Add New Voucher");
        model.addAttribute("isEdit", false);

        return "admin/form-voucher"; // Adjust path as needed
    }

    // Process add voucher
    @PostMapping("/add")
    public String addVoucher(@ModelAttribute VoucherDTO voucherDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            voucherService.createVoucher(voucherDTO);
            redirectAttributes.addFlashAttribute("success", "Voucher created successfully");
            return "redirect:/admin/vouchers";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("voucher", voucherDTO);
            return "redirect:/admin/vouchers/add";
        }
    }

    // Show edit voucher form - FIXED VERSION
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Voucher> voucherOpt = voucherService.getVoucherById(id);
        if (!voucherOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Voucher not found");
            return "redirect:/admin/vouchers";
        }

        Voucher voucher = voucherOpt.get();
        VoucherDTO voucherDTO = new VoucherDTO(
                voucher.getPromotionID(),
                voucher.getPromotionCode(),
                voucher.getDiscount(),
                voucher.getStartTime(),
                voucher.getEndTime(),
                voucher.getQuantity(),
                voucher.getStatus().toString() // FIXED: Convert enum to String
        );

        model.addAttribute("voucher", voucherDTO);
        model.addAttribute("pageTitle", "Edit Voucher");
        model.addAttribute("isEdit", true);

        return "admin/form-voucher"; // Adjust path as needed
    }

    // Process edit voucher
    @PostMapping("/edit/{id}")
    public String editVoucher(@PathVariable Integer id,
                              @ModelAttribute VoucherDTO voucherDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            voucherService.updateVoucher(id, voucherDTO);
            redirectAttributes.addFlashAttribute("success", "Voucher updated successfully");
            return "redirect:/admin/vouchers";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("voucher", voucherDTO);
            return "redirect:/admin/vouchers/edit/" + id;
        }
    }

    // Delete voucher
    @PostMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("success", "Voucher deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    // Get active vouchers (API endpoint)
    @GetMapping("/api/active")
    @ResponseBody
    public List<Voucher> getActiveVouchers() {
        return voucherService.getActiveVouchers();
    }

    // Validate voucher code (API endpoint)
    @GetMapping("/api/validate/{code}")
    @ResponseBody
    public boolean validateVoucher(@PathVariable String code) {
        return voucherService.validateVoucher(code);
    }
}