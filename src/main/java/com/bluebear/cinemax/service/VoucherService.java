package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.VoucherDTO;
import com.bluebear.cinemax.entity.Voucher;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import com.bluebear.cinemax.repository.VoucherRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
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
        if (voucherDTO.getStartTime() != null && voucherDTO.getEndTime() != null) {
            if (voucherDTO.getStartTime().isAfter(voucherDTO.getEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
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
        if (voucher.getStartTime() != null && voucher.getEndTime() != null) {
            if (voucher.getStartTime().isAfter(voucher.getEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
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
        return voucherRepository.findActiveVouchers(Promotion_Status.Available, now);
    }

    // Get expired vouchers
    public List<Voucher> getExpiredVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findExpiredVouchers(now, Promotion_Status.Expired);
    }

    // Search vouchers
    public List<Voucher> searchVouchers(String keyword, String status) {
        Promotion_Status enumStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                enumStatus = Promotion_Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status, will search without status filter
            }
        }
        return voucherRepository.searchVouchers(keyword, enumStatus);
    }

    // Get vouchers by status
    public List<Voucher> getVouchersByStatus(String status) {
        try {
            Promotion_Status enumStatus = Promotion_Status.valueOf(status);
            return voucherRepository.findByStatus(enumStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    // Validate voucher code - FIXED: Added null checks for time fields
    public boolean validateVoucher(String promotionCode) {
        Optional<Voucher> voucherOpt = voucherRepository.findByPromotionCode(promotionCode);
        if (!voucherOpt.isPresent()) {
            return false;
        }

        Voucher voucher = voucherOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // Check if voucher is active, within time range, and has quantity available
        boolean isStatusValid = voucher.getStatus() == Promotion_Status.Available;
        boolean isQuantityValid = voucher.getQuantity() > 0;
        boolean isTimeValid = true;

        // Check time range only if both start and end times are set
        if (voucher.getStartTime() != null && voucher.getEndTime() != null) {
            isTimeValid = voucher.getStartTime().isBefore(now) && voucher.getEndTime().isAfter(now);
        } else if (voucher.getStartTime() != null) {
            isTimeValid = voucher.getStartTime().isBefore(now);
        } else if (voucher.getEndTime() != null) {
            isTimeValid = voucher.getEndTime().isAfter(now);
        }

        return isStatusValid && isQuantityValid && isTimeValid;
    }

    // Get vouchers ending soon - FIXED: Added null checks
    public List<Voucher> getVouchersEndingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusWeeks(1);
        return voucherRepository.findVouchersEndingSoon(Promotion_Status.Available, now, weekLater);
    }

    // Get voucher statistics
    public VoucherStats getVoucherStats() {
        long totalVouchers = voucherRepository.count();
        long activeVouchers = voucherRepository.countByStatus(Promotion_Status.Available);
        long expiredVouchers = voucherRepository.countByStatus(Promotion_Status.Expired);
        Double averageDiscount = voucherRepository.getAverageDiscount(Promotion_Status.Available);

        return new VoucherStats(totalVouchers, activeVouchers, expiredVouchers,
                averageDiscount != null ? averageDiscount : 0.0);
    }

    // Count vouchers by status
    public long countVouchersByStatus(String status) {
        try {
            Promotion_Status enumStatus = Promotion_Status.valueOf(status);
            return voucherRepository.countByStatus(enumStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    // Check if promotion code exists
    public boolean existsByPromotionCode(String promotionCode) {
        return voucherRepository.existsByPromotionCode(promotionCode);
    }

    // Use voucher (decrease quantity) - FIXED: Added null checks
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

    // Update expired vouchers (batch job) - FIXED: Added null checks for time
    public void updateExpiredVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> availableVouchers = voucherRepository.findByStatus(Promotion_Status.Available);

        for (Voucher voucher : availableVouchers) {
            boolean shouldExpire = false;

            // Check if voucher should be expired based on end time
            if (voucher.getEndTime() != null && voucher.getEndTime().isBefore(now)) {
                shouldExpire = true;
            }

            // Check if voucher should be expired based on quantity
            if (voucher.getQuantity() <= 0) {
                shouldExpire = true;
            }

            if (shouldExpire) {
                voucher.setStatus(Promotion_Status.Expired);
                voucherRepository.save(voucher);
            }
        }
    }

    // Helper method to convert String status to enum safely
    private Promotion_Status convertStringToStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            return Promotion_Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid promotion status: " + status +
                    ". Valid values are: " + java.util.Arrays.toString(Promotion_Status.values()));
        }
    }

    // Get vouchers by status enum (type-safe version)
    public List<Voucher> getVouchersByStatus(Promotion_Status status) {
        return voucherRepository.findByStatus(status);
    }

    // Count vouchers by status enum (type-safe version)
    public long countVouchersByStatus(Promotion_Status status) {
        return voucherRepository.countByStatus(status);
    }

    // Additional helper methods for better API
    public List<Voucher> getAvailableVouchers() {
        return voucherRepository.findByStatus(Promotion_Status.Available);
    }

    public List<Voucher> getExpiredVouchersByStatus() {
        return voucherRepository.findByStatus(Promotion_Status.Expired);
    }

    public long countAvailableVouchers() {
        return voucherRepository.countByStatus(Promotion_Status.Available);
    }

    public long countExpiredVouchers() {
        return voucherRepository.countByStatus(Promotion_Status.Expired);
    }

    // Get voucher status for display - NEW METHOD
    public String getVoucherStatus(Voucher voucher) {
        if (voucher == null) {
            return "UNKNOWN";
        }

        LocalDateTime now = LocalDateTime.now();

        // Check if out of stock
        if (voucher.getQuantity() <= 0) {
            return "OUT_OF_STOCK";
        }

        // Check if expired by system status
        if (voucher.getStatus() == Promotion_Status.Expired) {
            return "EXPIRED";
        }

        // Check if expired by end time
        if (voucher.getEndTime() != null && voucher.getEndTime().isBefore(now)) {
            return "EXPIRED";
        }

        // Check if not started yet
        if (voucher.getStartTime() != null && voucher.getStartTime().isAfter(now)) {
            return "PENDING";
        }

        // Check if active
        if (voucher.getStatus() == Promotion_Status.Available) {
            return "ACTIVE";
        }

        return "UNKNOWN";
    }

    // Calculate usage statistics - NEW METHOD
    public VoucherUsageStats calculateUsageStats(Voucher voucher, Integer originalQuantity) {
        if (voucher == null) {
            return new VoucherUsageStats(0, 0, 0, 0.0);
        }

        int original = originalQuantity != null ? originalQuantity : voucher.getQuantity();
        int current = voucher.getQuantity();
        int used = Math.max(0, original - current);
        double percentage = original > 0 ? (double) used / original * 100 : 0.0;

        return new VoucherUsageStats(original, used, current, percentage);
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

        // Format discount as percentage
        public String getFormattedAverageDiscount() {
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(averageDiscount) + "%";
        }
    }

    // Inner class for usage statistics
    @Data
    public static class VoucherUsageStats {
        private int originalQuantity;
        private int usedQuantity;
        private int currentQuantity;
        private double usagePercentage;

        public VoucherUsageStats(int originalQuantity, int usedQuantity, int currentQuantity, double usagePercentage) {
            this.originalQuantity = originalQuantity;
            this.usedQuantity = usedQuantity;
            this.currentQuantity = currentQuantity;
            this.usagePercentage = usagePercentage;
        }

        public String getFormattedUsagePercentage() {
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(usagePercentage) + "%";
        }
    }
}