package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Promotion_Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionID")
    private Integer promotionID;

    @Column(name = "PromotionCode", nullable = false, length = 10, unique = true)
    private String promotionCode;

    @Column(name = "Discount", nullable = false)
    private Integer discount; // Giá trị giảm giá (ví dụ: phần trăm hoặc số tiền cố định)

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity; // Số lượng mã khuyến mãi có sẵn

    // Sử dụng Enum để quản lý trạng thái, giúp mã sạch hơn và an toàn kiểu dữ liệu
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private Promotion_Status status; // Enum PromotionStatus sẽ được định nghĩa bên dưới

    @OneToMany(mappedBy = "promotion")
    private List<Invoice> InvoiceList;
}