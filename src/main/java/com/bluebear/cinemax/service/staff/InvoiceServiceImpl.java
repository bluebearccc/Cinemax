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

    private Invoice convertToEntity(InvoiceDTO invoiceDTO) {
        if (invoiceDTO == null) {
            return null;
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceID(invoiceDTO.getId());
        invoice.setBookingDate(invoiceDTO.getBookingDate());
        invoice.setTotalPrice(invoiceDTO.getTotalPrice());

        if (invoiceDTO.getCustomerID() != null) {
            customerRepository.findById(invoiceDTO.getCustomerID())
                    .ifPresent(invoice::setCustomer);
        } else {
            invoice.setCustomer(null);
        }

        if (invoiceDTO.getEmployeeID() != null) {
            employeeRepository.findById(invoiceDTO.getEmployeeID())
                    .ifPresent(invoice::setEmployee);
        } else {
            invoice.setEmployee(null);
        }

        if (invoiceDTO.getPromotionID() != null) {
            promotionRepository.findById(invoiceDTO.getPromotionID())
                    .ifPresent(invoice::setPromotion);
        } else {
            invoice.setPromotion(null);
        }
        return invoice;
    }

    private Detail_FDDTO convertDetailFDToDTO(Detail_FD detail_FD) {
        if (detail_FD == null) {
            return null;
        }

        String itemName = null;
        if (detail_FD.getTheaterStock() != null) {
            itemName = detail_FD.getTheaterStock().getItemName();
        }

        LocalDateTime bookingDate = null;
        if (detail_FD.getInvoice() != null) {
            bookingDate = detail_FD.getInvoice().getBookingDate();
        }

        return Detail_FDDTO.builder()
                .id(detail_FD.getId())
                .invoiceId(detail_FD.getInvoice() != null ? detail_FD.getInvoice().getInvoiceID() : null)
                .theaterStockId(detail_FD.getTheaterStock() != null ? detail_FD.getTheaterStock().getStockID() : null)
                .quantity(detail_FD.getQuantity())
                .totalPrice(detail_FD.getTotalPrice())
                .itemName(itemName)
                .bookingDate(bookingDate)
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

