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

    // Get active vouchers
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

        if (!voucherDTO.isValid()) {
            throw new IllegalArgumentException("Invalid voucher data");
        }

        Voucher voucher = existingVoucher.get();

        // Check if promotion code is being changed and if new code already exists
        if (!voucher.getPromotionCode().equals(voucherDTO.getPromotionCode()) &&
                voucherRepository.existsByPromotionCode(voucherDTO.getPromotionCode())) {
            throw new IllegalArgumentException("Promotion code already exists");
        }

        voucher.setPromotionCode(voucherDTO.getPromotionCode());
        voucher.setDiscount(voucherDTO.getDiscount());
        voucher.setStartTime(voucherDTO.getStartTime());
        voucher.setEndTime(voucherDTO.getEndTime());
        voucher.setQuantity(voucherDTO.getQuantity());
        voucher.setStatus(voucherDTO.getStatus());

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
        return voucher.get().isActive();
    }

    // Use voucher (decrease quantity)
    public boolean useVoucher(String code) {
        Optional<Voucher> voucherOpt = voucherRepository.findByPromotionCode(code);
        if (!voucherOpt.isPresent()) {
            return false;
        }

        Voucher voucher = voucherOpt.get();
        if (!voucher.isActive()) {
            return false;
        }

        voucher.setQuantity(voucher.getQuantity() - 1);
        voucherRepository.save(voucher);
        return true;
    }

    // Get voucher statistics
    public VoucherStats getVoucherStats() {
        long totalVouchers = voucherRepository.count();
        long activeVouchers = voucherRepository.countByStatus("Active");
        long expiredVouchers = voucherRepository.findExpiredVouchers(LocalDateTime.now()).size();
        Double avgDiscount = voucherRepository.getAverageDiscount();

        return new VoucherStats(totalVouchers, activeVouchers, expiredVouchers,
                avgDiscount != null ? avgDiscount : 0.0);
    }

    // Inner class for statistics
    public static class VoucherStats {
        private long totalVouchers;
        private long activeVouchers;
        private long expiredVouchers;
        private double averageDiscount;

        public VoucherStats(long totalVouchers, long activeVouchers, long expiredVouchers, double averageDiscount) {
            this.totalVouchers = totalVouchers;
            this.activeVouchers = activeVouchers;
            this.expiredVouchers = expiredVouchers;
            this.averageDiscount = averageDiscount;
        }

        // Getters
        public long getTotalVouchers() { return totalVouchers; }
        public long getActiveVouchers() { return activeVouchers; }
        public long getExpiredVouchers() { return expiredVouchers; }
        public double getAverageDiscount() { return averageDiscount; }
        public String getFormattedAverageDiscount() {
            return String.format("%.1f%%", averageDiscount);
        }
    }
}