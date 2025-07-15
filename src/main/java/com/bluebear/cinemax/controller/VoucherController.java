package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.VoucherDTO;
import com.bluebear.cinemax.entity.Promotion;
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
        List<Promotion> vouchers = voucherService.getAllVouchers();
        VoucherService.VoucherStats stats = voucherService.getVoucherStats();

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Voucher Management");

        return "admin/list-voucher";
    }

    // Search vouchers
    @GetMapping("/search")
    public String searchVouchers(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 Model model) {
        List<Promotion> vouchers = voucherService.searchVouchers(keyword, status);
        VoucherService.VoucherStats stats = voucherService.getVoucherStats();

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("stats", stats);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pageTitle", "Voucher Search Results");

        return "admin/list-voucher";
    }

    // Show voucher details
    @GetMapping("/{id}")
    public String viewVoucher(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Promotion> voucher = voucherService.getVoucherById(id);
        if (!voucher.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Voucher not found");
            return "redirect:/admin/vouchers";
        }

        model.addAttribute("voucher", voucher.get());
        model.addAttribute("pageTitle", "Voucher Details");

        return "admin/detail-voucher";
    }

    // Show add voucher form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("voucher", new VoucherDTO());
        model.addAttribute("pageTitle", "Add New Voucher");
        model.addAttribute("isEdit", false);

        return "admin/add-voucher"; // Sửa để trả về template add
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
        Optional<Promotion> voucherOpt = voucherService.getVoucherById(id);
        if (!voucherOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Voucher not found");
            return "redirect:/admin/vouchers";
        }

        Promotion voucher = voucherOpt.get();
        VoucherDTO voucherDTO = new VoucherDTO(
                voucher.getPromotionID(),
                voucher.getPromotionCode(),
                voucher.getDiscount(),
                voucher.getStartTime(),
                voucher.getEndTime(),
                voucher.getQuantity(),
                voucher.getStatus().toString()
        );

        model.addAttribute("voucher", voucherDTO);
        model.addAttribute("pageTitle", "Edit Voucher");
        model.addAttribute("isEdit", true);

        return "admin/edit-voucher"; // Sửa để trả về template edit
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
    public List<Promotion> getActiveVouchers() {
        return voucherService.getActiveVouchers();
    }

    // Validate voucher code (API endpoint)
    @GetMapping("/api/validate/{code}")
    @ResponseBody
    public boolean validateVoucher(@PathVariable String code) {
        return voucherService.validateVoucher(code);
    }
}