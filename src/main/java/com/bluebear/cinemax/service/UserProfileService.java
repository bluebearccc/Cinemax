package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.dto.WatchedMovieDTO;
import com.bluebear.cinemax.entity.*;

import com.bluebear.cinemax.enumtype.InvoiceStatus;
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
    public CustomerDTO toDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .accountID(customer.getAccount() != null ? customer.getAccount().getId() : null)
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .point(customer.getPoint())
                .email(customer.getAccount() != null ? customer.getAccount().getEmail() : null)
                .build();
    }

    public AccountDTO toDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .email(account.getEmail())
                .password(account.getPassword())
                .role(account.getRole())
                .status(account.getStatus())
                .build();
    }

    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public void saveCustomer(CustomerDTO dto) {
        Customer customer = customerRepository.findById(dto.getId()).orElseThrow();
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());
        customer.setPoint(dto.getPoint());

        customerRepository.save(customer);
    }

    public AccountDTO getAccountById(Long id) {
        return accountRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public boolean emailExists(String email) {
        return accountRepository.findByEmail(email) != null;
    }

    public void saveAccount(AccountDTO dto) {
        Account account = accountRepository.findById(Long.valueOf(dto.getId())).orElseThrow();
        account.setEmail(dto.getEmail());
        account.setPassword(dto.getPassword());
        account.setStatus(dto.getStatus());
        account.setRole(dto.getRole());
        accountRepository.save(account);
    }

    public Customer getCustomerByAccount(Account account) {
        return customerRepository.findByAccount(account);
    }

    public List<InvoiceDTO> getBookedInvoicesByCustomer(CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(customerDTO.getId()).orElse(null);
        if (customer == null) return List.of();

        List<Invoice> invoices = invoiceRepository.findByCustomerAndStatus(customer, InvoiceStatus.Booked);

        return invoices.stream().map(invoice -> {
            InvoiceDTO dto = new InvoiceDTO();
            dto.setInvoiceId(invoice.getInvoiceID());
            dto.setBookingDate(invoice.getBookingDate());
            dto.setTotalPrice(invoice.getTotalPrice());
            dto.setDiscount(invoice.getDiscount());
            dto.setStatus(invoice.getStatus()); // hoặc giữ nguyên enum nếu DTO dùng enum
            return dto;
        }).collect(Collectors.toList());
    }


    public boolean hasWatchedMovies(CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(customerDTO.getId()).orElse(null);
        if (customer == null) return false;

        return invoiceRepository.existsByCustomerAndStatus(customer, InvoiceStatus.Booked);
    }


    public CustomerDTO getCustomerById(Integer id) {
        return customerRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public Theater getTheaterById(Integer theaterId) {
        return theaterRepository.findById(theaterId).orElse(null);
    }

    public List<WatchedMovieDTO> getWatchedMovies(CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(customerDTO.getId()).orElse(null);
        if (customer == null) return List.of();

        List<Invoice> bookedInvoices = invoiceRepository.findByCustomerAndStatus(customer, InvoiceStatus.Booked);

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
