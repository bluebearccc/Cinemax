package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.Detail_FDDTO;
import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.entity.Customer; // Giả định có entity Customer
import com.bluebear.cinemax.entity.Detail_FD; // Giả định có entity Detail_FD
import com.bluebear.cinemax.entity.Employee; // Giả định có entity Employee
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.entity.Promotion; // Giả định có entity Promotion
import com.bluebear.cinemax.repository.CustomerRepository; // Giả định có CustomerRepository
import com.bluebear.cinemax.repository.Detail_FDRepository; // Giả định có Detail_FDRepository
import com.bluebear.cinemax.repository.EmployeeRepository; // Giả định có EmployeeRepository
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.repository.PromotionRepository; // Giả định có PromotionRepository
import com.bluebear.cinemax.repository.TheaterStockRepository; // Cần thiết để tạo Detail_FDDTO đầy đủ

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository; // Inject CustomerRepository

    @Autowired
    private EmployeeRepository employeeRepository; // Inject EmployeeRepository

    @Autowired
    private PromotionRepository promotionRepository; // Inject PromotionRepository

    @Autowired
    private Detail_FDRepository detailFDRepository; // Inject Detail_FDRepository

    @Autowired
    private TheaterStockRepository theaterStockRepository; // Để lấy itemName cho Detail_FDDTO

    /**
     * Chuyển đổi một Invoice entity thành InvoiceDTO.
     * Bao gồm việc chuyển đổi danh sách Detail_FD liên quan sang Detail_FDDTOs.
     *
     * @param invoice Entity Invoice cần chuyển đổi.
     * @return InvoiceDTO đã chuyển đổi.
     */
    private InvoiceDTO convertToDTO(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceDTO invoiceDTO = InvoiceDTO.builder()
                .id(invoice.getInvoiceID())
                .customerID(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null)
                .EmployeeID(invoice.getEmployee() != null ? invoice.getEmployee().getId() : null)
                .promotionID(invoice.getPromotion() != null ? invoice.getPromotion().getPromotionID() : null)
                .bookingDate(invoice.getBookingDate())
                .totalPrice(invoice.getTotalPrice())
                .build();

        // Chuyển đổi danh sách Detail_FD sang Detail_FDDTOs
        if (invoice.getDetail_FD() != null) {
            invoiceDTO.setDetail_FDDTO(invoice.getDetail_FD().stream()
                    .map(this::convertDetailFDToDTO)
                    .collect(Collectors.toList()));
        }

        return invoiceDTO;
    }

    /**
     * Chuyển đổi một InvoiceDTO thành Invoice entity.
     * Không xử lý danh sách Detail_FD ở đây, chúng sẽ được quản lý bởi InvoiceService riêng.
     *
     * @param invoiceDTO DTO Invoice cần chuyển đổi.
     * @return Invoice entity đã chuyển đổi.
     */
    private Invoice convertToEntity(InvoiceDTO invoiceDTO) {
        if (invoiceDTO == null) {
            return null;
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceID(invoiceDTO.getId());
        invoice.setBookingDate(invoiceDTO.getBookingDate());
        invoice.setTotalPrice(invoiceDTO.getTotalPrice());

        // Set Customer
        if (invoiceDTO.getCustomerID() != null) {
            customerRepository.findById(invoiceDTO.getCustomerID())
                    .ifPresent(invoice::setCustomer);
        } else {
            invoice.setCustomer(null);
        }

        // Set Employee
        if (invoiceDTO.getEmployeeID() != null) {
            employeeRepository.findById(invoiceDTO.getEmployeeID())
                    .ifPresent(invoice::setEmployee);
        } else {
            invoice.setEmployee(null);
        }

        // Set Promotion
        if (invoiceDTO.getPromotionID() != null) {
            promotionRepository.findById(invoiceDTO.getPromotionID())
                    .ifPresent(invoice::setPromotion);
        } else {
            invoice.setPromotion(null);
        }

        // Quan trọng: Đối với danh sách Detail_FD, thường bạn sẽ không cập nhật chúng trực tiếp
        // thông qua InvoiceService khi chuyển đổi DTO sang Entity.
        // Thay vào đó, chúng sẽ được quản lý bởi DetailFD_Service riêng biệt.
        // Nếu bạn muốn lưu/cập nhật Detail_FD cùng lúc, bạn sẽ cần logic phức tạp hơn ở đây
        // để xử lý các đối tượng con.

        return invoice;
    }

    /**
     * Helper method to convert Detail_FD entity to Detail_FDDTO.
     * Populates itemName and bookingDate for the DTO.
     * This might be a simplified version depending on your actual Detail_FD entity and needs.
     */
    private Detail_FDDTO convertDetailFDToDTO(Detail_FD detail_FD) {
        if (detail_FD == null) {
            return null;
        }

        String itemName = null;
        if (detail_FD.getTheaterStock() != null) {
            // Fetch item name from associated TheaterStock entity
            itemName = detail_FD.getTheaterStock().getItemName();
        }

        LocalDateTime bookingDate = null;
        if (detail_FD.getInvoice() != null) {
            // Fetch booking date from associated Invoice entity (this will be the parent invoice)
            bookingDate = detail_FD.getInvoice().getBookingDate();
        }

        return Detail_FDDTO.builder()
                .id(detail_FD.getId())
                .invoiceId(detail_FD.getInvoice() != null ? detail_FD.getInvoice().getInvoiceID() : null)
                .theaterStockId(detail_FD.getTheaterStock() != null ? detail_FD.getTheaterStock().getStockID() : null)
                .quantity(detail_FD.getQuantity())
                .totalPrice(detail_FD.getTotalPrice())
                .itemName(itemName) // Set the item name
                .bookingDate(bookingDate) // Set the booking date
                .build();
    }


    @Override
    public InvoiceDTO getInvoiceById(Integer id) {
        Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);
        return invoiceOptional.map(this::convertToDTO).orElse(null);
    }

    @Override
    public InvoiceDTO getInvoiceByDetailFDId(Integer id) {
        return null;
    }

}

