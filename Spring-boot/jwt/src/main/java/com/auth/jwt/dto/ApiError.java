package com.auth.jwt.dto;

import java.time.LocalDateTime;

public record ApiError(int status, String message, LocalDateTime timestamp) {
}
