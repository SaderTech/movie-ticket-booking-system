package com.movieticket.notificationservice.application.command;

public record CreateTemplateCommand(
        String code,
        String subject,
        String body
) {
}