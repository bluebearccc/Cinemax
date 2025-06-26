package com.bluebear.cinemax.service;
import com.bluebear.cinemax.config.VnpayConfig;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.InvoiceRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bluebear.cinemax.service.BookingService;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import com.bluebear.cinemax.repository.TheaterStockRepository;
@Service
@RequiredArgsConstructor
public class VnpayService {
    private final InvoiceRepository invoiceRepo;
    private final VnpayConfig vnpayConfig;
    private final BookingService bookingService;
    private final TheaterStockRepository theaterStockRepo;
    public String createPaymentUrl(Invoice invoice, HttpServletRequest request) {
        if (invoice.getTotalPrice() == null) {
            throw new IllegalStateException("Invoice chưa có tổng tiền.");
        }

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long vnp_Amount = invoice.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue();
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
    public Invoice getPendingInvoiceById(int id) {
        return invoiceRepo.findByInvoiceIdAndStatus(id, InvoiceStatus.Booked)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang chờ thanh toán"));
    }


}
