package com.movieticket.bookingservice.infrastructure.adapter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class VnPayUtil {

    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final String VERSION = "2.1.0";
    private static final String COMMAND = "pay";
    private static final String ORDER_TYPE = "other";
    private static final String CURR_CODE = "VND";
    private static final String LOCALE = "vn";

    private static final java.util.Map<String, String> RESPONSE_CODES = java.util.Map.ofEntries(
            java.util.Map.entry("00", "Giao dịch thành công"),
            java.util.Map.entry("07", "Trừ tiền thành công - Giao dịch bị nghi ngờ (liên quan tới lừa đảo)"),
            java.util.Map.entry("09", "Giao dịch thất bại - Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng"),
            java.util.Map.entry("10", "Giao dịch thất bại - Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần"),
            java.util.Map.entry("11", "Giao dịch thất bại - Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch"),
            java.util.Map.entry("12", "Giao dịch thất bại - Thẻ/Tài khoản của khách hàng bị khóa"),
            java.util.Map.entry("13", "Giao dịch thất bại - Khách hàng nhập sai mật khẩu xác thực giao dịch (OTP). Xin vui lòng thực hiện lại giao dịch"),
            java.util.Map.entry("24", "Giao dịch thất bại - Khách hàng hủy giao dịch"),
            java.util.Map.entry("51", "Giao dịch thất bại - Tài khoản của khách hàng không đủ số dư để thực hiện giao dịch"),
            java.util.Map.entry("65", "Giao dịch thất bại - Tài khoản của khách hàng đã vượt quá hạn mức giao dịch trong ngày"),
            java.util.Map.entry("75", "Ngân hàng thanh toán đang bảo trì"),
            java.util.Map.entry("79", "Giao dịch thất bại - Khách hàng nhập sai mật khẩu thanh toán quá số lần quy định. Xin vui lòng thực hiện lại giao dịch"),
            java.util.Map.entry("99", "Các lỗi khác (khách hàng hủy hoặc lỗi không xác định)")
    );

    public static String getResponseCodeDescription(String code) {
        return RESPONSE_CODES.getOrDefault(code, "Mã lỗi không xác định: " + code);
    }

    public static String buildPaymentUrl(String vnpUrl, String tmnCode, String hashSecret,
                                          String txnRef, long amount, String orderInfo,
                                          String returnUrl, String ipAddress) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", VERSION);
        params.put("vnp_Command", COMMAND);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", CURR_CODE);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", ORDER_TYPE);
        params.put("vnp_Locale", LOCALE);
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String hashData = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));

        String secureHash = hmacSha512(hashSecret, hashData);
        params.put("vnp_SecureHash", secureHash);

        String query = params.entrySet().stream()
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));

        return vnpUrl + "?" + query;
    }

    public static boolean verifyReturn(String hashSecret, Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null) return false;

        Map<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("vnp_SecureHash");
        sortedParams.remove("vnp_SecureHashType");

        String hashData = sortedParams.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));

        String computedHash = hmacSha512(hashSecret, hashData);
        return computedHash.equals(secureHash);
    }

    private static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            mac.init(spec);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC SHA512 error", e);
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
