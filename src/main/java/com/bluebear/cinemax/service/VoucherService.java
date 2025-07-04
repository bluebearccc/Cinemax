package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.VoucherDTO;
import com.bluebear.cinemax.entity.Voucher;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import com.bluebear.cinemax.repository.VoucherRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
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
    public Optional<Voucher> getVoucherByPromotionCode(String promotionCode) {
        return voucherRepository.findByPromotionCode(promotionCode);
    }

    // Create new voucher
    public Voucher createVoucher(VoucherDTO voucherDTO) {
        if (!voucherDTO.isValid()) {
            throw new IllegalArgumentException("Invalid voucher data");
        }

        // Check if promotion code already exists
        if (voucherRepository.existsByPromotionCode(voucherDTO.getPromotionCode())) {
            throw new IllegalArgumentException("Promotion code already exists");
        }

        // Validate date range
        if (voucherDTO.getStartTime().isAfter(voucherDTO.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Create voucher entity
        Voucher voucher = Voucher.builder()
                .promotionCode(voucherDTO.getPromotionCode())
                .discount(voucherDTO.getDiscount())
                .startTime(voucherDTO.getStartTime())
                .endTime(voucherDTO.getEndTime())
                .quantity(voucherDTO.getQuantity())
                .status(Promotion_Status.valueOf(voucherDTO.getStatus()))
                .build();

        return voucherRepository.save(voucher);
    }

    // Update voucher
    public Voucher updateVoucher(Integer id, VoucherDTO voucherDTO) {
        if (!voucherDTO.isValidForUpdate()) {
            throw new IllegalArgumentException("Invalid voucher data for update");
        }

        Optional<Voucher> voucherOpt = voucherRepository.findById(id);
        if (!voucherOpt.isPresent()) {
            throw new IllegalArgumentException("Voucher not found");
        }

        Voucher voucher = voucherOpt.get();

        // Check if promotion code is being changed and if it already exists
        if (voucherDTO.getPromotionCode() != null &&
                !voucherDTO.getPromotionCode().equals(voucher.getPromotionCode()) &&
                voucherRepository.existsByPromotionCode(voucherDTO.getPromotionCode())) {
            throw new IllegalArgumentException("Promotion code already exists");
        }

        // Update fields if provided
        if (voucherDTO.getPromotionCode() != null) {
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
        if (voucherDTO.getStatus() != null) {
            voucher.setStatus(Promotion_Status.valueOf(voucherDTO.getStatus()));
        }

        // Validate date range after update
        if (voucher.getStartTime().isAfter(voucher.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        return voucherRepository.save(voucher);
    }

    // Delete voucher
    public void deleteVoucher(Integer id) {
        Optional<Voucher> voucherOpt = voucherRepository.findById(id);
        if (!voucherOpt.isPresent()) {
            throw new IllegalArgumentException("Voucher not found");
        }
        voucherRepository.deleteById(id);
    }

    // Get active vouchers
    public List<Voucher> getActiveVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findActiveVouchers(now);
    }

    // Get expired vouchers
    public List<Voucher> getExpiredVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findExpiredVouchers(now);
    }

    // Search vouchers
    public List<Voucher> searchVouchers(String keyword, String status) {
        return voucherRepository.searchVouchers(keyword, status);
    }

    // Get vouchers by status
    public List<Voucher> getVouchersByStatus(String status) {
        return voucherRepository.findByStatus(status);
    }

    // Validate voucher code
    public boolean validateVoucher(String promotionCode) {
        Optional<Voucher> voucherOpt = voucherRepository.findByPromotionCode(promotionCode);
        if (!voucherOpt.isPresent()) {
            return false;
        }

        Voucher voucher = voucherOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // Check if voucher is active, within time range, and has quantity available
        return voucher.getStatus() == Promotion_Status.Available &&
                voucher.getStartTime().isBefore(now) &&
                voucher.getEndTime().isAfter(now) &&
                voucher.getQuantity() > 0;
    }

    // Get vouchers ending soon
    public List<Voucher> getVouchersEndingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusWeeks(1);
        return voucherRepository.findVouchersEndingSoon(now, weekLater);
    }

    // Get voucher statistics
    public VoucherStats getVoucherStats() {
        long totalVouchers = voucherRepository.count();
        long activeVouchers = voucherRepository.countByStatus("Available");
        long expiredVouchers = voucherRepository.countByStatus("Expired");
        Double averageDiscount = voucherRepository.getAverageDiscount();

        return new VoucherStats(totalVouchers, activeVouchers, expiredVouchers,
                averageDiscount != null ? averageDiscount : 0.0);
    }

    // Count vouchers by status
    public long countVouchersByStatus(String status) {
        return voucherRepository.countByStatus(status);
    }

    // Check if promotion code exists
    public boolean existsByPromotionCode(String promotionCode) {
        return voucherRepository.existsByPromotionCode(promotionCode);
    }

    // Use voucher (decrease quantity)
    public boolean useVoucher(String promotionCode) {
        Optional<Voucher> voucherOpt = voucherRepository.findByPromotionCode(promotionCode);
        if (!voucherOpt.isPresent()) {
            return false;
        }

        Voucher voucher = voucherOpt.get();
        if (!validateVoucher(promotionCode)) {
            return false;
        }

        // Decrease quantity
        voucher.setQuantity(voucher.getQuantity() - 1);

        // If quantity becomes 0, set status to Expired
        if (voucher.getQuantity() <= 0) {
            voucher.setStatus(Promotion_Status.Expired);
        }

        voucherRepository.save(voucher);
        return true;
    }

    // Update expired vouchers (batch job)
    public void updateExpiredVouchers() {
        List<Voucher> expiredVouchers = getExpiredVouchers();
        for (Voucher voucher : expiredVouchers) {
            if (voucher.getStatus() == Promotion_Status.Available) {
                voucher.setStatus(Promotion_Status.Expired);
                voucherRepository.save(voucher);
            }
        }
    }

    // Inner class for voucher statistics
    @Data
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
    }
}