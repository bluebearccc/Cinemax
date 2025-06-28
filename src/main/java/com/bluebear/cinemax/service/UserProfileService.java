package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.dto.WatchedMovieDTO;
import com.bluebear.cinemax.entity.*;

import com.bluebear.cinemax.enumtype.Invoice_Status;
import com.bluebear.cinemax.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private DetailSeatRepository detailSeatRepository;
    @Autowired
    private TheaterRepository theaterRepository;
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
    public void saveCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setID(customerDTO.getId());
        customer.setPhone(customerDTO.getPhone());

        if (customerDTO.getAccountID() != null) {
            Account account = accountRepository.findById(customerDTO.getAccountID().longValue()).orElse(null);
            customer.setAccount(account);
        }

        // không gọi hàm `toEntity()` mà tạo trực tiếp như trên
        customerRepository.save(customer);
    }


    public AccountDTO getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) return null;

        return new AccountDTO(
                account.getId(),
                account.getEmail(),
                account.getPassword(),
                account.getRole(),
                account.getStatus()
        );
    }

    public boolean emailExists(String email) {
        return accountRepository.findByEmail(email) != null;
    }

    public void saveAccount(AccountDTO accountDTO) {
        Account account = new Account();

        account.setId(accountDTO.getId());
        account.setEmail(accountDTO.getEmail());
        account.setPassword(accountDTO.getPassword());
        account.setRole(accountDTO.getRole());
        account.setStatus(accountDTO.getStatus());

        accountRepository.save(account);
    }


    public Customer getCustomerByAccount(Account account) {
        return customerRepository.findByAccount(account);
    }

    public List<InvoiceDTO> getBookedInvoicesByCustomer(CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(customerDTO.getId()).orElse(null);
        if (customer == null) return List.of();

        List<Invoice> invoices = invoiceRepository.findByCustomerAndStatus(customer, Invoice_Status.Booked);

        return invoices.stream().map(invoice -> {
            InvoiceDTO dto = new InvoiceDTO();
            dto.setInvoiceId(invoice.getInvoiceId());
            dto.setBookingDate(invoice.getBookingDate());
            dto.setTotalprice(invoice.getTotalPrice());
            dto.setDiscount(invoice.getDiscount());
            dto.setStatus(invoice.getStatus()); // hoặc giữ nguyên enum nếu DTO dùng enum
            return dto;
        }).collect(Collectors.toList());
    }


    public boolean hasWatchedMovies(CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(customerDTO.getId()).orElse(null);
        if (customer == null) return false;

        return invoiceRepository.existsByCustomerAndStatus(customer, Invoice_Status.Booked);
    }


    public CustomerDTO getCustomerById(Integer customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) return null;

        return CustomerDTO.builder()
                .id(customer.getID())
                .accountID(customer.getAccount().getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .build();
    }

    public Theater getTheaterById(Integer theaterId) {
        return theaterRepository.findById(theaterId).orElse(null);
    }

    public List<WatchedMovieDTO> getWatchedMovies(CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(customerDTO.getId()).orElse(null);
        if (customer == null) return List.of();

        List<Invoice> bookedInvoices = invoiceRepository.findByCustomerAndStatus(customer, Invoice_Status.Booked);

        return bookedInvoices.stream()
                .flatMap(invoice -> invoice.getDetailSeats().stream())
                .filter(detailSeat -> detailSeat.getSchedule().getEndTime().isBefore(LocalDateTime.now()))
                .map(detailSeat -> {
                    Movie movie = detailSeat.getSchedule().getMovie();
                    Theater theater = detailSeat.getSchedule().getRoom().getTheater();

                    return new WatchedMovieDTO(movie, theater);
                })
                .distinct() // tránh bị trùng nếu nhiều ghế trong cùng suất chiếu
                .collect(Collectors.toList());
    }

}
