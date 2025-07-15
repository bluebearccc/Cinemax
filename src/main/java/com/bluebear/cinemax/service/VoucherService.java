package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.VoucherDTO;
import com.bluebear.cinemax.entity.Promotion;
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
    public List<Promotion> getAllVouchers() {
        return voucherRepository.findAll();
    }

    // Get voucher by ID
    public Optional<Promotion> getVoucherById(Integer id) {
        return voucherRepository.findById(id);
    }

    // Create new voucher
    public Promotion createVoucher(VoucherDTO voucherDTO) {
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
        Promotion voucher = Promotion.builder()
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
    public Promotion updateVoucher(Integer id, VoucherDTO voucherDTO) {
        if (!voucherDTO.isValidForUpdate()) {
            throw new IllegalArgumentException("Invalid voucher data for update");
        }

        Optional<Promotion> voucherOpt = voucherRepository.findById(id);
        if (!voucherOpt.isPresent()) {
            throw new IllegalArgumentException("Voucher not found");
        }

        Promotion voucher = voucherOpt.get();

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
        Optional<Promotion> voucherOpt = voucherRepository.findById(id);
        if (!voucherOpt.isPresent()) {
            throw new IllegalArgumentException("Voucher not found");
        }
        voucherRepository.deleteById(id);
    }

    // Get active vouchers
    public List<Promotion> getActiveVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findActiveVouchers(Promotion_Status.Available, now);
    }

    // Get expired vouchers
    public List<Promotion> getExpiredVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findExpiredVouchers(now, Promotion_Status.Expired);
    }

    // Search vouchers
    public List<Promotion> searchVouchers(String keyword, String status) {
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

    // Validate voucher code - FIXED: Added null checks for time fields
    public boolean validateVoucher(String promotionCode) {
        Optional<Promotion> voucherOpt = voucherRepository.findByPromotionCode(promotionCode);
        if (!voucherOpt.isPresent()) {
            return false;
        }

        Promotion voucher = voucherOpt.get();
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


    // Get voucher statistics
    public VoucherStats getVoucherStats() {
        long totalVouchers = voucherRepository.count();
        long activeVouchers = voucherRepository.countByStatus(Promotion_Status.Available);
        long expiredVouchers = voucherRepository.countByStatus(Promotion_Status.Expired);
        Double averageDiscount = voucherRepository.getAverageDiscount(Promotion_Status.Available);

        return new VoucherStats(totalVouchers, activeVouchers, expiredVouchers,
                averageDiscount != null ? averageDiscount : 0.0);
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