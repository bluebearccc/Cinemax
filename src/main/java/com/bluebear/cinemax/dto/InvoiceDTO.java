package com.bluebear.cinemax.dto;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.enumtype.Invoice_Status;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InvoiceDTO {

    private Integer invoiceId;
    private Integer customerId;
    private Integer employeeId;
    private Integer promotionId;
    private Float discount;
    private LocalDateTime bookingDate;
    private Double totalprice;
    private Invoice_Status status;
    private List<DetailSeatDTO> detailSeats;
    public InvoiceDTO(Invoice invoice) {
        this.invoiceId = invoice.getInvoiceId();
        this.customerId = invoice.getCustomer() != null ? invoice.getCustomer().getID() : null;
        this.employeeId = invoice.getEmployee() != null ? invoice.getEmployee().getId() : null;
        this.promotionId=invoice.getPromotion() != null ? invoice.getPromotion().getPromotionID() : null;
        this.discount=invoice.getDiscount();
        this.bookingDate=invoice.getBookingDate();
        this.totalprice=invoice.getTotalPrice();

    }



}
