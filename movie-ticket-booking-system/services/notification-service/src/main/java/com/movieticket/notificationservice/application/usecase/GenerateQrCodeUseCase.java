package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.infrastructure.qrcode.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateQrCodeUseCase {

    private final QrCodeGenerator qrCodeGenerator;

    public byte[] execute(String ticketCode) {
        return qrCodeGenerator.generate(ticketCode);
    }
}