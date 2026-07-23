package com.movieticket.bookingservice.infrastructure.adapter;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class VnPayUtilTest {

    private static final String TMN_CODE = "TU8GEVIB";
    private static final String HASH_SECRET = "LENI87G7NEE3466DIWN9M0AXDXDV1PN5";
    private static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String RETURN_URL = "http://localhost:8085/api/bookings/vnpay-return";
    private static final String IP_ADDRESS = "127.0.0.1";

    @Test
    void buildPaymentUrl_containsRequiredParams() {
        String paymentUrl = VnPayUtil.buildPaymentUrl(
                VNP_URL, TMN_CODE, HASH_SECRET,
                "TXN_TEST123", 180000L, "Thanh toan ve xem phim",
                RETURN_URL, IP_ADDRESS);

        assertNotNull(paymentUrl);
        assertTrue(paymentUrl.startsWith(VNP_URL));
        assertTrue(paymentUrl.contains("vnp_TmnCode=" + TMN_CODE));
        assertTrue(paymentUrl.contains("vnp_TxnRef=TXN_TEST123"));
        assertTrue(paymentUrl.contains("vnp_Amount=18000000"));
        assertTrue(paymentUrl.contains("vnp_SecureHash"));
        assertTrue(paymentUrl.contains("vnp_ReturnUrl=" + java.net.URLEncoder.encode(RETURN_URL, StandardCharsets.UTF_8)));
    }

    @Test
    void verifyReturn_validHash_returnsTrue() {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_TmnCode", TMN_CODE);
        params.put("vnp_TxnRef", "TXN_ROUNDTRIP");
        params.put("vnp_Amount", "9000000");
        params.put("vnp_OrderInfo", "Test payment");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");

        String hashData = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + java.net.URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        String expectedHash = hmacSha512(HASH_SECRET, hashData);
        params.put("vnp_SecureHash", expectedHash);

        assertTrue(VnPayUtil.verifyReturn(HASH_SECRET, params));
    }

    @Test
    void buildPaymentUrl_signsUrlEncodedValues() {
        String paymentUrl = VnPayUtil.buildPaymentUrl(
                VNP_URL, TMN_CODE, HASH_SECRET,
                "TXN_ENCODED", 180000L, "Thanh toan ve xem phim",
                RETURN_URL, IP_ADDRESS);

        Map<String, String> params = parseQueryParams(paymentUrl);
        String secureHash = params.remove("vnp_SecureHash");
        String hashData = new TreeMap<>(params).entrySet().stream()
                .map(e -> e.getKey() + "=" + java.net.URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        assertEquals(hmacSha512(HASH_SECRET, hashData), secureHash);
    }

    @Test
    void verifyReturn_tamperedHash_returnsFalse() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TmnCode", TMN_CODE);
        params.put("vnp_TxnRef", "TXN_TEST789");
        params.put("vnp_Amount", "18000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_SecureHash", "INVALID_HASH_VALUE");

        assertFalse(VnPayUtil.verifyReturn(HASH_SECRET, params));
    }

    @Test
    void verifyReturn_missingHash_returnsFalse() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TXN_TEST999");

        assertFalse(VnPayUtil.verifyReturn(HASH_SECRET, params));
    }

    private Map<String, String> parseQueryParams(String url) {
        Map<String, String> params = new LinkedHashMap<>();
        String query = url.contains("?") ? url.substring(url.indexOf("?") + 1) : url;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                params.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
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
}
