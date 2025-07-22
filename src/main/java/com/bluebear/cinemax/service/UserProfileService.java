package com.bluebear.cinemax.service;
import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.dto.Movie.InvoiceDetailDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.service.serviceFeedback.ServiceFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    @Autowired
    private FeedbackServiceRepository serviceFeedbackRepository;
    @Autowired
    private ServiceFeedbackService serviceFeedbackService;
    @Autowired
    private DetailFDRepository detailFDRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

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

    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public void saveCustomer(CustomerDTO dto) {
        Customer customer = customerRepository.findById(dto.getId()).orElseThrow();
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());
        customer.setPoint(dto.getPoint());

        customerRepository.save(customer);
    }

    public AccountDTO getAccountById(Integer id) {
        return accountRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public boolean emailExists(String email) {
        return accountRepository.findByEmail(email) != null;
    }

    public void saveAccount(AccountDTO dto) {
        Account account = accountRepository.findById(dto.getId()).orElseThrow();
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
            dto.setInvoiceID(invoice.getInvoiceID());
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
                .map(this::toWatchedMovieDTO) // sử dụng hàm toDTO thay vì constructor
                .distinct() // nếu cần loại trùng, cần override equals/hashCode
                .collect(Collectors.toList());
    }

    public InvoiceDetailDTO getInvoiceDetailById(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();

        DetailSeat firstSeat = invoice.getDetailSeats().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Invoice has no seats"));

        Room room = firstSeat.getSchedule().getRoom();
        Theater theater = room.getTheater();

        List<String> seatNames = invoice.getDetailSeats()
                .stream()
                .map(ds -> ds.getSeat().getPosition())
                .collect(Collectors.toList());

        List<String> foodNames = invoice.getDetail_FD()
                .stream()
                .map(detailFD -> {
                    TheaterStock stock = detailFD.getTheaterStock();
                    return stock != null ? stock.getItemName() : "Không rõ";
                })
                .collect(Collectors.toList());

        return InvoiceDetailDTO.builder()
                .invoiceId(invoice.getInvoiceID())
                .theaterName(theater.getTheaterName())
                .roomName(room.getName())
                .bookingDate(invoice.getBookingDate())
                .seats(seatNames)
                .foodName(foodNames)
                .theaterstock(foodNames)
                .totalPrice(invoice.getTotalPrice())
                .discount(invoice.getDiscount())
                .status(invoice.getStatus())
                .build();
    }
    public WatchedMovieDTO toWatchedMovieDTO(DetailSeat detailSeat) {
        Schedule schedule = detailSeat.getSchedule();
        Movie movie = schedule.getMovie();
        Theater theater = schedule.getRoom().getTheater();
        Integer invoiceId = detailSeat.getInvoice().getInvoiceID();

        return WatchedMovieDTO.builder()
                .movie(movie)
                .theater(theater)
                .schedule(schedule)
                .invoiceId(invoiceId) // BẮT BUỘC phải có dòng này
                .build();
    }
    public ServiceFeedbackDTO prepareFeedbackFromInvoice(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        Customer customer = invoice.getCustomer();
        Integer theaterId = invoice.getDetailSeats().get(0)
                .getSchedule().getRoom().getTheater().getTheaterID();

        ServiceFeedbackDTO dto = new ServiceFeedbackDTO();
        dto.setCustomerId(customer.getId());
        dto.setTheaterId(theaterId);
        return dto;
    }


    public void submitFeedback(ServiceFeedbackDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        FeedbackService feedback = new FeedbackService();
        feedback.setCustomer(customer);
        feedback.setContent(dto.getContent());
        feedback.setCreatedDate(LocalDateTime.now());
        feedback.setServiceRate(dto.getServiceRate());
        feedback.setTheaterId(dto.getTheaterId());

        // ✅ Logic được chuyển từ controller xuống đây:
        if (dto.getServiceRate() < 4) {
            feedback.setStatus(FeedbackStatus.Not_Suported);
        } else {
            feedback.setStatus(FeedbackStatus.Suported);
        }

        serviceFeedbackRepository.save(feedback);
    }


}