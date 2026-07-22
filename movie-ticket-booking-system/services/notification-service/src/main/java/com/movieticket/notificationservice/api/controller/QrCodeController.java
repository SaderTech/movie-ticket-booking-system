package com.movieticket.notificationservice.api.controller;

import com.movieticket.notificationservice.application.usecase.GenerateQrCodeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPath.QR_CODES)
@RequiredArgsConstructor
public class QrCodeController {

    private final GenerateQrCodeUseCase generateQrCodeUseCase;

    @GetMapping(
            value = ApiPath.TICKET,
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public byte[] generateTicketQr(@PathVariable("ticketCode") String ticketCode) {
        return generateQrCodeUseCase.execute(ticketCode);
    }
}