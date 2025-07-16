package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import com.bluebear.cinemax.repository.PromotionRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository voucherRepository;

    // ==================== VALIDATION METHODS ====================

    // Validation for create mode (all fields required)
    public boolean isValidForCreate(PromotionDTO voucherDTO) {
        return voucherDTO.getPromotionCode() != null && !voucherDTO.getPromotionCode().trim().isEmpty() &&
                voucherDTO.getDiscount() != null && voucherDTO.getDiscount() >= 0 && voucherDTO.getDiscount() <= 100 &&
                voucherDTO.getStartTime() != null && voucherDTO.getEndTime() != null && voucherDTO.getStartTime().isBefore(voucherDTO.getEndTime()) &&
                voucherDTO.getQuantity() != null && voucherDTO.getQuantity() >= 0 &&
                voucherDTO.getStatus() != null && !voucherDTO.getStatus().trim().isEmpty() &&
                isValidStatus(voucherDTO.getStatus());
    }

    // Validation for edit mode (some fields can be null)
    public boolean isValidForUpdate(PromotionDTO voucherDTO) {
        // If promotion code is provided, it must be valid
        if (voucherDTO.getPromotionCode() != null && voucherDTO.getPromotionCode().trim().isEmpty()) {
            return false;
        }

        // If discount is provided, it must be between 0-100
        if (voucherDTO.getDiscount() != null && (voucherDTO.getDiscount() < 0 || voucherDTO.getDiscount() > 100)) {
            return false;
        }

        // If both start and end time are provided, start must be before end
        if (voucherDTO.getStartTime() != null && voucherDTO.getEndTime() != null && !voucherDTO.getStartTime().isBefore(voucherDTO.getEndTime())) {
            return false;
        }

        // If quantity is provided, it must be >= 0
        if (voucherDTO.getQuantity() != null && voucherDTO.getQuantity() < 0) {
            return false;
        }

        // If status is provided, it must be valid
        if (voucherDTO.getStatus() != null && !voucherDTO.getStatus().trim().isEmpty() && !isValidStatus(voucherDTO.getStatus())) {
            return false;
        }

        return true;
    }

    // Helper method to validate status
    private boolean isValidStatus(String status) {
        return "Available".equals(status) || "Expired".equals(status);
    }

    // ==================== BUSINESS METHODS ====================

    // Get all vouchers
    public List<Promotion> getAllVouchers() {
        return voucherRepository.findAll();
    }

    // Get voucher by ID
    public Optional<Promotion> getVoucherById(Integer id) {
        return voucherRepository.findById(id);
    }

    // Create new voucher
    public Promotion createVoucher(PromotionDTO voucherDTO) {
        if (!isValidForCreate(voucherDTO)) {
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
    public Promotion updateVoucher(Integer id, PromotionDTO voucherDTO) {
        if (!isValidForUpdate(voucherDTO)) {
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


    // Get total vouchers count
    public long getTotalVouchersCount() {
        return voucherRepository.count();
    }

    // Get active vouchers count
    public long getActiveVouchersCount() {
        return voucherRepository.countByStatus(Promotion_Status.Available);
    }

    // Get expired vouchers count
    public long getExpiredVouchersCount() {
        return voucherRepository.countByStatus(Promotion_Status.Expired);
    }

    // Get average discount for active vouchers
    public double getAverageDiscountForActiveVouchers() {
        Double averageDiscount = voucherRepository.getAverageDiscount(Promotion_Status.Available);
        return averageDiscount != null ? averageDiscount : 0.0;
    }
}