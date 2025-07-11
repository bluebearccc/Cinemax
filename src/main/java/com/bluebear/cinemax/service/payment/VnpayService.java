package com.bluebear.cinemax.service.payment;
import com.bluebear.cinemax.config.VnpayConfig;
import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.service.booking.BookingServiceImp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VnpayService {
    private final DetailSeatRepository detailSeatRepo;
    private final ScheduleRepository scheduleRepo;
    private final SeatRepository seatRepo;
    private final InvoiceRepository invoiceRepo;
    private final VnpayConfig vnpayConfig;
    private final BookingServiceImp bookingServiceImp;
    private final TheaterStockRepository theaterStockRepo;
    public String createPaymentUrl(InvoiceDTO invoice, HttpServletRequest request) {
        if (invoice.getTotalPrice() == null) {
            throw new IllegalStateException("Invoice chưa có tổng tiền.");
        }

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long vnp_Amount = Math.round(invoice.getTotalPrice() * 100);
        String vnp_TxnRef = String.valueOf(invoice.getInvoiceId());
        String vnp_IpAddr = getIpAddress(request);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toán hóa đơn #" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_Locale", "vn");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", createDate);
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", expireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = Optional.ofNullable(vnp_Params.get(fieldName)).orElse(""); // fix null

            hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

            if (i < fieldNames.size() - 1) {
                hashData.append('&');
                query.append('&');
            }
        }

        String vnp_SecureHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        String finalUrl = vnpayConfig.getPayUrl() + "?" + query.toString();
        System.out.println("VNPAY Redirect URL: " + finalUrl); // optional log
        return finalUrl;
    }


    private String getRandomNumber(int length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        return (ip != null) ? ip : request.getRemoteAddr();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo HMAC SHA512", e);
        }
    }
    public InvoiceDTO getInvoiceDTOById(Integer invoiceId) {

            Invoice invoice = invoiceRepo.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            InvoiceDTO dto = new InvoiceDTO();
            dto.setInvoiceId(invoice.getInvoiceID());
            dto.setCustomerId(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null);
            dto.setEmployeeId(invoice.getEmployee() != null ? invoice.getEmployee().getId() : null);
            dto.setPromotionId(invoice.getPromotion() != null ? invoice.getPromotion().getPromotionID() : null);
            dto.setBookingDate(invoice.getBookingDate());
            dto.setDiscount(invoice.getDiscount());
            dto.setTotalPrice(invoice.getTotalPrice() != null ? invoice.getTotalPrice().doubleValue() : null);

            // Map danh sách ghế
            List<DetailSeatDTO> seatDTOs = invoice.getDetailSeats().stream().map(ds -> {
                DetailSeatDTO detailSeatDTO = new DetailSeatDTO();
                detailSeatDTO.setId(ds.getId());
                detailSeatDTO.setInvoiceID(invoice.getInvoiceID());
                detailSeatDTO.setSeatID(ds.getSeat().getSeatID());
                detailSeatDTO.setScheduleID(ds.getSchedule().getScheduleID());
                return detailSeatDTO;
            }).collect(Collectors.toList());
            dto.setDetailSeats(seatDTOs);

            return dto;
        }
    public ScheduleDTO getScheduleDTO(Integer scheduleId) {
        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch chiếu"));

        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleID(schedule.getScheduleID());
        dto.setStartTime(schedule.getStartTime());

        // Movie
        Movie movie = schedule.getMovie();
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setMovieID(movie.getMovieID());
        movieDTO.setMovieName(movie.getMovieName());
        dto.setMovie(movieDTO);

        // Room
        Room room = schedule.getRoom();
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomID(room.getRoomID());
        roomDTO.setName(room.getName());
        dto.setRoom(roomDTO);

        return dto;
    }
    public String getSeatPosition(Integer seatId) {
        return seatRepo.findById(seatId)
                .map(Seat::getPosition)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế"));
    }
    @Transactional
    public void confirmInvoiceAfterPayment(int invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Chỉ cập nhật nếu trạng thái hiện tại là UNPAID hoặc PENDING
        if (invoice.getStatus() != InvoiceStatus.Booked) {
            invoice.setStatus(InvoiceStatus.Booked);
            invoiceRepo.save(invoice);
        }

        // Cập nhật tất cả các DetailSeat thuộc hóa đơn này
        List<DetailSeat> detailSeats = detailSeatRepo.findByInvoiceInvoiceID(invoiceId);
        for (DetailSeat seat : detailSeats) {
            seat.setStatus(DetailSeat_Status.Booked); // Enum bạn đã tạo
        }
        detailSeatRepo.saveAll(detailSeats);
    }

}
