package com.bluebear.cinemax.controller.admin;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/vouchers")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    // Display all vouchers
    @GetMapping
    public String listVouchers(Model model) {
        List<Promotion> vouchers = promotionService.getAllVouchers();

        // Lấy từng thống kê riêng lẻ thay vì dùng VoucherStats object
        long totalVouchers = promotionService.getTotalVouchersCount();
        long activeVouchers = promotionService.getActiveVouchersCount();
        long expiredVouchers = promotionService.getExpiredVouchersCount();
        double averageDiscount = promotionService.getAverageDiscountForActiveVouchers();

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("activeVouchers", activeVouchers);
        model.addAttribute("expiredVouchers", expiredVouchers);
        model.addAttribute("averageDiscount", averageDiscount);
        model.addAttribute("activeLink", "vouchers");

        model.addAttribute("pageTitle", "Voucher Management");

        return "admin/list-voucher";
    }

    // Search vouchers
    @GetMapping("/search")
    public String searchVouchers(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 Model model) {
        List<Promotion> vouchers = promotionService.searchVouchers(keyword, status);

        // Lấy thống kê cho trang search
        long totalVouchers = promotionService.getTotalVouchersCount();
        long activeVouchers = promotionService.getActiveVouchersCount();
        long expiredVouchers = promotionService.getExpiredVouchersCount();
        double averageDiscount = promotionService.getAverageDiscountForActiveVouchers();

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("activeVouchers", activeVouchers);
        model.addAttribute("expiredVouchers", expiredVouchers);
        model.addAttribute("averageDiscount", averageDiscount);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("activeLink", "vouchers");

        model.addAttribute("pageTitle", "Voucher Search Results");

        return "admin/list-voucher";
    }

    // Show voucher details
    @GetMapping("/{id}")
    public String viewVoucher(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Promotion> voucher = promotionService.getVoucherById(id);
        if (!voucher.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Voucher not found");
            return "redirect:/admin/vouchers";
        }

        model.addAttribute("voucher", voucher.get());
        model.addAttribute("activeLink", "vouchers");

        model.addAttribute("pageTitle", "Voucher Details");

        return "admin/detail-voucher";
    }

    // Show add voucher form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("voucher", new PromotionDTO());
        model.addAttribute("activeLink", "vouchers");
        model.addAttribute("pageTitle", "Add New Voucher");
        model.addAttribute("isEdit", false);

        return "admin/form-voucher";
    }

    // Process add voucher - FIX URL
    @PostMapping("/add")
    public String addVoucher(@ModelAttribute PromotionDTO voucherDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            promotionService.createVoucher(voucherDTO);
            redirectAttributes.addFlashAttribute("success", "Voucher created successfully");
            return "redirect:/admin/vouchers";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("voucher", voucherDTO);
            // FIX: Redirect về đúng đường dẫn form
            return "redirect:/admin/vouchers/add";
        }
    }

    // Show edit voucher form - FIX: Xử lý đúng kiểu dữ liệu
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Promotion> voucherOpt = promotionService.getVoucherById(id);
        if (!voucherOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Voucher not found");
            return "redirect:/admin/vouchers";
        }

        Promotion voucher = voucherOpt.get();
        PromotionDTO voucherDTO = new PromotionDTO(
                voucher.getPromotionID(),
                voucher.getPromotionCode(),
                voucher.getDiscount(),
                voucher.getStartTime(),
                voucher.getEndTime(),
                voucher.getQuantity(),
                voucher.getStatus()// FIX: Convert enum to String
        );

        // Kiểm tra nếu có voucher từ redirect (khi có lỗi)
        if (!model.containsAttribute("voucher")) {
            model.addAttribute("voucher", voucherDTO);
        }
        model.addAttribute("pageTitle", "Edit Voucher");
        model.addAttribute("isEdit", true);
        model.addAttribute("activeLink", "vouchers");

        return "admin/form-voucher";
    }
    // Process edit voucher - FIX URL
    @PostMapping("/edit/{id}")
    public String editVoucher(@PathVariable Integer id,
                              @ModelAttribute PromotionDTO voucherDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            promotionService.updateVoucher(id, voucherDTO);
            redirectAttributes.addFlashAttribute("success", "Voucher updated successfully");
            return "redirect:/admin/vouchers";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("voucher", voucherDTO);
            // FIX: Redirect về đúng đường dẫn edit
            return "redirect:/admin/vouchers/edit/" + id;
        }
    }

    // Delete voucher
    @PostMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            promotionService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("success", "Voucher deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }
}