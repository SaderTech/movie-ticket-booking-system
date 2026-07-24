package com.movieticket.notificationservice.infrastructure.mail;

public record EmailAttachment(
        String filename,
        byte[] content,
        String contentType
) {
}
