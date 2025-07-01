package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.VoucherDTO;
import com.bluebear.cinemax.entity.Voucher;
import com.bluebear.cinemax.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    // Get all vouchers
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    // Get voucher by ID
    public Optional<Voucher> getVoucherById(Integer id) {
        return voucherRepository.findById(id);
    }

    // Get voucher by promotion code
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByPromotionCode(code);
    }

    // Search vouchers
    public List<Voucher> searchVouchers(String keyword, String status) {
        return voucherRepository.searchVouchers(keyword, status);
    }

    // Get vouchers by status
    public List<Voucher> getVouchersByStatus(String status) {
        return voucherRepository.findByStatus(status);
    }

    // Get active vouchers (Available and currently active)
    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findActiveVouchers(LocalDateTime.now());
    }

    // Get expired vouchers
    public List<Voucher> getExpiredVouchers() {
        return voucherRepository.findExpiredVouchers(LocalDateTime.now());
    }

    // Create new voucher
    public Voucher createVoucher(VoucherDTO voucherDTO) {
        if (!voucherDTO.isValid()) {
            throw new IllegalArgumentException("Invalid voucher data");
        }

        if (voucherRepository.existsByPromotionCode(voucherDTO.getPromotionCode())) {
            throw new IllegalArgumentException("Promotion code already exists");
        }

        Voucher voucher = new Voucher();
        voucher.setPromotionCode(voucherDTO.getPromotionCode());
        voucher.setDiscount(voucherDTO.getDiscount());
        voucher.setStartTime(voucherDTO.getStartTime());
        voucher.setEndTime(voucherDTO.getEndTime());
        voucher.setQuantity(voucherDTO.getQuantity());
        voucher.setStatus(voucherDTO.getStatus());

        return voucherRepository.save(voucher);
    }

    // Update voucher
    public Voucher updateVoucher(Integer id, VoucherDTO voucherDTO) {
        Optional<Voucher> existingVoucher = voucherRepository.findById(id);
        if (!existingVoucher.isPresent()) {
            throw new IllegalArgumentException("Voucher not found");
        }

        Voucher voucher = existingVoucher.get();

        // Check if promotion code is being changed and if new code already exists
        if (voucherDTO.getPromotionCode() != null &&
                !voucher.getPromotionCode().equals(voucherDTO.getPromotionCode()) &&
                voucherRepository.existsByPromotionCode(voucherDTO.getPromotionCode())) {
            throw new IllegalArgumentException("Promotion code already exists");
        }

        // Update only fields that are provided in DTO (not null/empty)
        if (voucherDTO.getPromotionCode() != null && !voucherDTO.getPromotionCode().trim().isEmpty()) {
            voucher.setPromotionCode(voucherDTO.getPromotionCode());
        }

        if (voucherDTO.getDiscount() != null) {
            voucher.setDiscount(voucherDTO.getDiscount());
        }

        if (voucherDTO.getStartTime() != null) {
            voucher.setStartTime(voucherDTO.getStartTime());
        }

        if (voucherDTO.getEndTime() != null) {
            voucher.setEndTime(voucherDTO.getEndTime());
        }

        if (voucherDTO.getQuantity() != null) {
            voucher.setQuantity(voucherDTO.getQuantity());
        }

        if (voucherDTO.getStatus() != null && !voucherDTO.getStatus().trim().isEmpty()) {
            voucher.setStatus(voucherDTO.getStatus());
        }

        return voucherRepository.save(voucher);
    }

    // Delete voucher
    public void deleteVoucher(Integer id) {
        if (!voucherRepository.existsById(id)) {
            throw new IllegalArgumentException("Voucher not found");
        }
        voucherRepository.deleteById(id);
    }

    // Validate voucher for use
    public boolean validateVoucher(String code) {
        Optional<Voucher> voucher = voucherRepository.findByPromotionCode(code);
        if (!voucher.isPresent()) {
            return false;
        }
        return voucher.get().isUsable(); // Use new isUsable() method
    }

    // Use voucher (decrease quantity)
    public boolean useVoucher(String code) {
        Optional<Voucher> voucherOpt = voucherRepository.findByPromotionCode(code);
        if (!voucherOpt.isPresent()) {
            return false;
        }

        Voucher voucher = voucherOpt.get();
        if (!voucher.isUsable()) { // Use new isUsable() method
            return false;
        }

        voucher.setQuantity(voucher.getQuantity() - 1);
        voucherRepository.save(voucher);
        return true;
    }

    // Get voucher statistics - Updated for Available/Expired
    public VoucherStats getVoucherStats() {
        long totalVouchers = voucherRepository.count();
        long availableVouchers = voucherRepository.countByStatus("Available"); // Changed from "Active"
        long expiredVouchers = voucherRepository.countByStatus("Expired"); // Direct count instead of findExpiredVouchers
        Double avgDiscount = voucherRepository.getAverageDiscount();

        return new VoucherStats(totalVouchers, availableVouchers, expiredVouchers,
                avgDiscount != null ? avgDiscount : 0.0);
    }

    // Inner class for statistics - Updated naming
    public static class VoucherStats {
        private long totalVouchers;
        private long availableVouchers; // Changed from activeVouchers
        private long expiredVouchers;
        private double averageDiscount;

        public VoucherStats(long totalVouchers, long availableVouchers, long expiredVouchers, double averageDiscount) {
            this.totalVouchers = totalVouchers;
            this.availableVouchers = availableVouchers;
            this.expiredVouchers = expiredVouchers;
            this.averageDiscount = averageDiscount;
        }

        // Getters - Updated naming
        public long getTotalVouchers() { return totalVouchers; }
        public long getAvailableVouchers() { return availableVouchers; } // Changed from getActiveVouchers
        public long getExpiredVouchers() { return expiredVouchers; }
        public double getAverageDiscount() { return averageDiscount; }
        public String getFormattedAverageDiscount() {
            return String.format("%.1f%%", averageDiscount);
        }

        // Backward compatibility
        public long getActiveVouchers() { return availableVouchers; } // For templates that still use activeVouchers
    }
}