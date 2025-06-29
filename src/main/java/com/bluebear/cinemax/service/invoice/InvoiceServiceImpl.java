package com.bluebear.cinemax.service.invoice;

import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.Employee;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.EmployeeRepository;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService{
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PromotionRepository promotionRepository;

    
    public InvoiceDTO createInvoice(InvoiceDTO dto) {
        Invoice invoice = toEntity(dto);
        return toDTO(invoiceRepository.save(invoice));
    }

    public InvoiceDTO getInvoiceById(Integer id) {
        return invoiceRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public InvoiceDTO updateInvoice(Integer id, InvoiceDTO dto) {
        Optional<Invoice> optional = invoiceRepository.findById(id);
        if (optional.isPresent()) {
            Invoice updated = toEntity(dto);
            updated.setInvoiceID(id);
            return toDTO(invoiceRepository.save(updated));
        }
        return null;
    }
    
    public void deleteInvoice(Integer id) {
        invoiceRepository.deleteById(id);
    }

    public InvoiceDTO toDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .invoiceID(invoice.getInvoiceID())
                .customerID(invoice.getCustomer().getId())
                .employeeID(invoice.getEmployee() != null ? invoice.getEmployee().getId() : null)
                .promotionID(invoice.getPromotion() != null ? invoice.getPromotion().getPromotionID() : null)
                .discount(invoice.getDiscount())
                .bookingDate(invoice.getBookingDate())
                .totalPrice(invoice.getTotalPrice())
                .build();
    }

    public Invoice toEntity(InvoiceDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerID()).orElseThrow();
        Employee employee = null;
        if (dto.getEmployeeID() != null) {
            employee = employeeRepository.findById(dto.getEmployeeID()).orElse(null);
        }

        Promotion promotion = null;
        if (dto.getPromotionID() != null) {
            promotion = promotionRepository.findById(dto.getPromotionID()).orElse(null);
        }

        return Invoice.builder()
                .invoiceID(dto.getInvoiceID())
                .customer(customer)
                .employee(employee)
                .promotion(promotion)
                .discount(dto.getDiscount())
                .bookingDate(dto.getBookingDate())
                .totalPrice(dto.getTotalPrice())
                .build();
    }
}
