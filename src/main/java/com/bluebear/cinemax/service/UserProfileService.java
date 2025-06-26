package com.bluebear.cinemax.service;

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
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public boolean emailExists(String email) {
        return accountRepository.findByEmail(email) != null;
    }

    public void saveAccount(Account account) {
        accountRepository.save(account);
    }



    public Customer getCustomerByAccount(Account account) {
        return customerRepository.findByAccount(account);
    }

    public List<Invoice> getBookedInvoicesByCustomer(Customer customer) {
        return invoiceRepository.findByCustomerAndStatus(customer, InvoiceStatus.Booked);
    }

    public boolean hasWatchedMovies(Customer customer) {
        return invoiceRepository.existsByCustomerAndStatus(customer, InvoiceStatus.Booked);
    }

    public Customer getCustomerById(Integer customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }
    public Theater getTheaterById(Integer theaterId) {
        return theaterRepository.findById(theaterId).orElse(null);
    }
//    public List<Movie> getWatchedMovies(Customer customer) {
//        LocalDateTime now = LocalDateTime.now();
//
//        return getBookedInvoicesByCustomer(customer).stream()
//                .flatMap(invoice -> detailSeatRepository.findByInvoice(invoice).stream())
//                .filter(detailSeat -> detailSeat.getSchedule().getEndTime().isBefore(now))
//                .map(detailSeat -> detailSeat.getSchedule().getMovie())
//                .distinct()
//                .collect(Collectors.toList());
//    }
    public List<WatchedMovieDTO> getWatchedMovies(Customer customer) {
        List<Invoice> bookedInvoices = invoiceRepository.findByCustomerAndStatus(customer, InvoiceStatus.Booked);

        return bookedInvoices.stream()
                .flatMap(invoice -> invoice.getDetailSeats().stream())
                .filter(detailSeat -> detailSeat.getSchedule().getEndTime().isBefore(LocalDateTime.now()))
                .map(detailSeat -> {
                    Movie movie = detailSeat.getSchedule().getMovie();
                    Theater theater = detailSeat.getSchedule().getRoom().getTheater();
                    return new WatchedMovieDTO(movie, theater);
                })
                .distinct() // tránh trùng nếu nhiều ghế trong 1 suất
                .collect(Collectors.toList());
    }
}
